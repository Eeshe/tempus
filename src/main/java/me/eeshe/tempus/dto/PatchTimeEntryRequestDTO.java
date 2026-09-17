package me.eeshe.tempus.dto;

import java.time.Instant;

import org.openapitools.jackson.nullable.JsonNullable;

import jakarta.validation.constraints.NotNull;

public record PatchTimeEntryRequestDTO(
        @NotNull(message = ERROR_MESSAGE_NULL_PROJECT) JsonNullable<Long> projectId,
        JsonNullable<Long> taskId,
        JsonNullable<String> description,
        @NotNull(message = ERROR_MESSAGE_NULL_BILLABLE) JsonNullable<Boolean> isBillable,
        @NotNull(message = ERROR_MESSAGE_NULL_START_TIME) JsonNullable<Instant> startTime,
        JsonNullable<Instant> endTime) {
    private static final String ERROR_MESSAGE_NULL_PROJECT = "Time entry project can't be null if provided";
    private static final String ERROR_MESSAGE_NULL_BILLABLE = "Time entry billable status can't be null if provided";
    private static final String ERROR_MESSAGE_NULL_START_TIME = "Time entry start time can't be null if provided";
}
