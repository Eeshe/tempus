package me.eeshe.tempus.dto;

import java.time.LocalDate;
import java.util.List;

import org.openapitools.jackson.nullable.JsonNullable;

import jakarta.validation.constraints.NotNull;

public record ReportRequestDTO(
        @NotNull(message = ERROR_MESSAGE_NULL_START_DATE) LocalDate startDate,
        @NotNull(message = ERROR_MESSAGE_NULL_END_DATE) LocalDate endDate,
        @NotNull(message = ERROR_MESSAGE_NULL_PROJECTS) JsonNullable<List<Long>> projectIds,
        @NotNull(message = ERROR_MESSAGE_NULL_DESCRIPTIONS) JsonNullable<List<String>> descriptions,
        JsonNullable<Boolean> isBillable

) {
    private static final String ERROR_MESSAGE_NULL_START_DATE = "Report start date can't be null";
    private static final String ERROR_MESSAGE_NULL_END_DATE = "Report end date can't be null";
    private static final String ERROR_MESSAGE_NULL_PROJECTS = "Projects can't be null if provided";
    private static final String ERROR_MESSAGE_NULL_DESCRIPTIONS = "Descriptions can't be null if provided";
}
