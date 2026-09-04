package me.eeshe.tempus.dto;

import java.util.List;

public record ReportDTO(
        long totalTrackedTimeMillis,
        List<ProjectReportEntryDTO> projectReportEntries) {
}
