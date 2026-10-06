package me.eeshe.tempus.dto;

public record ProjectReportEntryDTO(
        ProjectDTO project,
        long trackedTimeMillis) {
}
