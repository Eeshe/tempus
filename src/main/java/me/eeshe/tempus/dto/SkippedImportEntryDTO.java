package me.eeshe.tempus.dto;

import java.time.LocalDate;

public record SkippedImportEntryDTO(
        String fileName,
        int rowNumber,
        LocalDate date,
        String reason) {
}
