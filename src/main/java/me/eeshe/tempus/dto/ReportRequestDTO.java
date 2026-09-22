package me.eeshe.tempus.dto;

import java.time.Instant;
import java.util.List;

public record ReportRequestDTO(
        Instant startDate,
        Instant endDate,
        List<Long> projectIds,
        List<Long> taskIds,
        List<Long> clientIds,
        List<String> descriptions,
        Boolean isBillable) {
}
