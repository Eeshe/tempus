package me.eeshe.tempus.dto;

import java.util.List;

public record ImportResultDTO(
        long importedCount,
        int skippedCount,
        List<SkippedImportEntryDTO> skippedEntries) {
}
