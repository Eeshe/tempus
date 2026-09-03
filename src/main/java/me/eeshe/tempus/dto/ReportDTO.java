package me.eeshe.tempus.dto;

import java.util.List;

public record ReportDTO(
        List<TimeEntryDTO> timeEntries,
        long totalTrackedTimeMillis) {
}
