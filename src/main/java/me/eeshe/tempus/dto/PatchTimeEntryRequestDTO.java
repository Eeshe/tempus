package me.eeshe.tempus.dto;

import org.openapitools.jackson.nullable.JsonNullable;

import jakarta.validation.constraints.NotNull;

public record PatchTimeEntryRequestDTO(
        JsonNullable<Long> groupId,
        @NotNull(message = ERROR_MESSAGE_NULL_PROJECT) JsonNullable<Long> projectId,
        @NotNull(message = ERROR_MESSAGE_NULL_TASK) JsonNullable<Long> taskId,
        JsonNullable<String> description,
        @NotNull(message = ERROR_MESSAGE_NULL_BILLABLE) JsonNullable<Boolean> isBillable) {
    private static final String ERROR_MESSAGE_NULL_PROJECT = "Time entry project can't be null if provided";
    private static final String ERROR_MESSAGE_NULL_TASK = "Time entry task can't be null if provided";
    private static final String ERROR_MESSAGE_NULL_BILLABLE = "Time entry billable status can't be null if provided";
}
