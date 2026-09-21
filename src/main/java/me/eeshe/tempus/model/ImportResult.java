package me.eeshe.tempus.model;

import java.util.List;

public record ImportResult(
        long importedCount,
        List<SkippedImportEntry> skippedEntries) {

    public int skippedCount() {
        return skippedEntries.size();
    }
}
