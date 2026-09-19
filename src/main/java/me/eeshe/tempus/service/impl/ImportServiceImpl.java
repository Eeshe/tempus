package me.eeshe.tempus.service.impl;

import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.opencsv.CSVReader;
import com.opencsv.bean.CsvToBeanBuilder;

import me.eeshe.tempus.exception.CSVTimeEntryImportException;
import me.eeshe.tempus.mapper.TimeEntryMapper;
import me.eeshe.tempus.model.CSVTimeEntry;
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
    public void importCSVFiles(long userId, ImportCSVFilesRequest importCSVFilesRequest) {
        final List<CreateTimeEntryRequest> validCreateRequests = new ArrayList<>();
        for (MultipartFile csvFile : importCSVFilesRequest.files()) {
            final List<CSVTimeEntry> csvTimeEntries = readCSVFile(csvFile);
            for (CSVTimeEntry csvTimeEntry : csvTimeEntries) {
                try {
                    validCreateRequests.add(timeEntryMapper.fromCSVTimeEntry(csvTimeEntry, userId));
                } catch (CSVTimeEntryImportException e) {
                    LOGGER.warn("Skipping row '{}' from file '{}':",
                            csvTimeEntry.getDate(),
                            csvFile.getOriginalFilename(),
                            e.getMessage());
                }
            }
        }
        timeEntryService.createTimeEntries(validCreateRequests);
    }

    private List<CSVTimeEntry> readCSVFile(MultipartFile csvFile) {
        try (CSVReader csvReader = new CSVReader(new InputStreamReader(csvFile.getInputStream()))) {
            return new CsvToBeanBuilder<CSVTimeEntry>(csvReader)
                    .withType(CSVTimeEntry.class)
                    .build().parse();
        } catch (IllegalStateException | IOException e) {
            e.printStackTrace();
        }
        return List.of();
    }
}
