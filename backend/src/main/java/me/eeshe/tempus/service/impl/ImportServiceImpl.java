package me.eeshe.tempus.service.impl;

import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.opencsv.CSVReader;
import com.opencsv.bean.CsvToBeanBuilder;

import me.eeshe.tempus.exception.CSVFileReadException;
import me.eeshe.tempus.exception.CSVTimeEntryImportException;
import me.eeshe.tempus.mapper.TimeEntryMapper;
import me.eeshe.tempus.model.CSVTimeEntry;
import me.eeshe.tempus.model.ImportResult;
import me.eeshe.tempus.model.SkippedImportEntry;
import me.eeshe.tempus.request.CreateTimeEntryRequest;
import me.eeshe.tempus.request.ImportCSVFilesRequest;
import me.eeshe.tempus.service.ImportService;
import me.eeshe.tempus.service.TimeEntryService;

@Service
public class ImportServiceImpl implements ImportService {
    private static final Logger LOGGER = LoggerFactory.getLogger(ImportServiceImpl.class);

    private final TimeEntryService timeEntryService;
    private final TimeEntryMapper timeEntryMapper;

    public ImportServiceImpl(TimeEntryService timeEntryService, TimeEntryMapper timeEntryMapper) {
        this.timeEntryService = timeEntryService;
        this.timeEntryMapper = timeEntryMapper;
    }

    @Override
    public ImportResult importCSVFiles(long userId, ImportCSVFilesRequest importCSVFilesRequest) {
        final List<CreateTimeEntryRequest> validCreateRequests = new ArrayList<>();
        final List<SkippedImportEntry> skippedEntries = new ArrayList<>();

        for (MultipartFile csvFile : importCSVFilesRequest.files()) {
            final String fileName = csvFile.getOriginalFilename();
            final List<CSVTimeEntry> csvTimeEntries;
            try {
                csvTimeEntries = readCSVFile(csvFile);
            } catch (CSVFileReadException e) {
                LOGGER.warn("Skipping CSV file import for '{}': {}", fileName, e.getMessage());
                skippedEntries.add(new SkippedImportEntry(fileName, 0, null, e.getMessage()));
                continue;
            }
            int rowNumber = 0;
            for (CSVTimeEntry csvTimeEntry : csvTimeEntries) {
                rowNumber++;
                final Optional<String> validationError = csvTimeEntry.getValidationError();
                if (validationError.isPresent()) {
                    LOGGER.warn("Skipping row '{}' from file '{}': {}",
                            rowNumber,
                            fileName,
                            validationError.get());
                    skippedEntries.add(new SkippedImportEntry(
                            fileName,
                            rowNumber,
                            csvTimeEntry.getDate(),
                            validationError.get()));
                    continue;
                }
                try {
                    validCreateRequests.add(timeEntryMapper.fromCSVTimeEntry(csvTimeEntry, userId));
                } catch (CSVTimeEntryImportException e) {
                    LOGGER.warn("Skipping row '{}' from file '{}': {}",
                            rowNumber,
                            fileName,
                            e.getMessage());
                    skippedEntries.add(new SkippedImportEntry(
                            fileName,
                            rowNumber,
                            csvTimeEntry.getDate(),
                            e.getMessage()));
                }
            }
        }

        final long importedCount = timeEntryService.createTimeEntries(validCreateRequests).size();

        return new ImportResult(importedCount, skippedEntries);
    }

    private List<CSVTimeEntry> readCSVFile(MultipartFile csvFile) {
        try (CSVReader csvReader = new CSVReader(new InputStreamReader(csvFile.getInputStream()))) {
            return new CsvToBeanBuilder<CSVTimeEntry>(csvReader)
                    .withType(CSVTimeEntry.class)
                    .build().parse();
        } catch (IllegalStateException | IOException e) {
            throw new CSVFileReadException("Could not read CSV file: " + e.getMessage(), e);
        }
    }
}
