package me.eeshe.tempus.dto;

import jakarta.validation.constraints.NotNull;

public record CreateTimeEntryRequestDTO(
        @NotNull(message = ERROR_MESSAGE_NULL_GROUP) Long groupId,
        @NotNull(message = ERROR_MESSAGE_NULL_USER) Long userId,
        @NotNull(message = ERROR_MESSAGE_NULL_PROJECT) Long projectId,
        Long taskId,
        String description,
        @NotNull(message = ERROR_MESSAGE_NULL_BILLABLE) Boolean isBillable) {
    private static final String ERROR_MESSAGE_NULL_GROUP = "Time entry group can't be null";
    private static final String ERROR_MESSAGE_NULL_USER = "Time entry user can't be null";
    private static final String ERROR_MESSAGE_NULL_PROJECT = "Time entry project can't be null";
    private static final String ERROR_MESSAGE_NULL_BILLABLE = "Time entry billable status can't be null";
}
