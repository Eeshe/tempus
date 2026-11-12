package me.eeshe.tempus.dto;

import java.util.List;

public record ProjectReportEntryDTO(
        ProjectDTO project,
        long trackedTimeMillis,
        List<ReportTimeEntryDTO> timeEntries) {
}
