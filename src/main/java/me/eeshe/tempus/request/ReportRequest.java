package me.eeshe.tempus.request;

import java.time.LocalDate;
import java.util.List;

import org.openapitools.jackson.nullable.JsonNullable;

public record ReportRequest(
        LocalDate startDate,
        LocalDate endDate,
        JsonNullable<List<Long>> projectIds,
        JsonNullable<List<String>> descriptions,
        JsonNullable<Boolean> isBillable) {
}
