package me.eeshe.tempus.dto;

import java.time.LocalDate;
import java.util.List;

import jakarta.validation.constraints.NotNull;

public record ReportRequestDTO(
        @NotNull(message = ERROR_MESSAGE_NULL_START_DATE) LocalDate startDate,
        @NotNull(message = ERROR_MESSAGE_NULL_END_DATE) LocalDate endDate,
        List<Long> projectIds,
        List<Long> taskIds,
        List<String> descriptions,
        Boolean isBillable

) {
    private static final String ERROR_MESSAGE_NULL_START_DATE = "Report start date can't be null";
    private static final String ERROR_MESSAGE_NULL_END_DATE = "Report end date can't be null";
}
