package me.eeshe.tempus.mapper.impl;

import org.springframework.stereotype.Component;

import me.eeshe.tempus.dto.ImportCSVFilesRequestDTO;
import me.eeshe.tempus.dto.ImportResultDTO;
import me.eeshe.tempus.dto.SkippedImportEntryDTO;
import me.eeshe.tempus.mapper.ImportMapper;
import me.eeshe.tempus.model.ImportResult;
import me.eeshe.tempus.model.SkippedImportEntry;
import me.eeshe.tempus.request.ImportCSVFilesRequest;

@Component
public class ImportMapperImpl implements ImportMapper {

    @Override
    public ImportCSVFilesRequest fromDTO(ImportCSVFilesRequestDTO importCSVFilesRequestDTO) {
        return new ImportCSVFilesRequest(importCSVFilesRequestDTO.files());
    }

    @Override
    public ImportResultDTO toDTO(ImportResult importResult) {
        return new ImportResultDTO(
                importResult.importedCount(),
                importResult.skippedCount(),
                importResult.skippedEntries().stream().map(this::toDTO).toList());
    }

    private SkippedImportEntryDTO toDTO(SkippedImportEntry skippedImportEntry) {
        return new SkippedImportEntryDTO(
                skippedImportEntry.fileName(),
                skippedImportEntry.rowNumber(),
                skippedImportEntry.date(),
                skippedImportEntry.reason());
    }
}
