package me.eeshe.tempus.request;

import java.time.Instant;
import java.util.List;

public record ReportRequest(
        long userId,
        Instant startDate,
        Instant endDate,
        List<Long> projectIds,
        List<Long> taskIds,
        List<Long> clientIds,
        List<String> descriptions,
        Boolean isBillable) {
}
