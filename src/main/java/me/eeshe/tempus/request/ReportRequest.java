package me.eeshe.tempus.request;

import java.time.LocalDate;
import java.util.List;

public record ReportRequest(
        LocalDate startDate,
        LocalDate endDate,
        List<Long> projectIds,
        List<String> descriptions,
        Boolean isBillable) {
}
