package me.eeshe.tempus.model;

import java.time.LocalDate;

public record SkippedImportEntry(
        String fileName,
        int rowNumber,
        LocalDate date,
        String reason) {
}
