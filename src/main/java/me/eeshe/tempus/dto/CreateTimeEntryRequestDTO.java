package me.eeshe.tempus.dto;

import java.time.Instant;

import jakarta.validation.constraints.NotNull;

public record CreateTimeEntryRequestDTO(
        @NotNull(message = ERROR_MESSAGE_NULL_PROJECT) Long projectId,
        Long taskId,
        String description,
        @NotNull(message = ERROR_MESSAGE_NULL_BILLABLE) Boolean isBillable,
        @NotNull(message = ERROR_MESSAGE_NULL_START_TIME) Instant startTime,
        Instant endTime) {
    private static final String ERROR_MESSAGE_NULL_PROJECT = "Time entry project can't be null";
    private static final String ERROR_MESSAGE_NULL_BILLABLE = "Time entry billable status can't be null";
    private static final String ERROR_MESSAGE_NULL_START_TIME = "Time entry start time can't be null";
}
