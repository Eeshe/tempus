package me.eeshe.tempus.dto;

public record ClientReportEntryDTO(
        ClientDTO client,
        long trackedTimeMillis) {
}
