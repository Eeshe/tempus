package me.eeshe.tempus.dto;

import java.time.Instant;

public record ReportTimeEntryDTO(
        long id,
        String description,
        boolean isBillable,
        String taskName,
        Instant startTime,
        Instant endTime) {
}
