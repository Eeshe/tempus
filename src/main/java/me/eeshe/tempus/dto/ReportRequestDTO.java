package me.eeshe.tempus.dto;

import java.time.LocalDate;
import java.util.List;

public record ReportRequestDTO(
        LocalDate startDate,
        LocalDate endDate,
        List<Long> projectIds,
        List<Long> taskIds,
        List<Long> clientIds,
        List<String> descriptions,
        Boolean isBillable) {
}
