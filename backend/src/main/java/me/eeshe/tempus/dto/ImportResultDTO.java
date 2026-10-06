package me.eeshe.tempus.dto;

import java.util.List;

public record ImportResultDTO(
        long importedCount,
        long skippedCount,
        List<SkippedImportEntryDTO> skippedEntries) {
}
