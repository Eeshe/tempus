package me.eeshe.tempus.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

public record TimeEntryPatchDTO(
        @NotNull(message = ERROR_MESSAGE_NULL_TIME_ENTRY_ID) Long timeEntryId,
        @NotNull(message = ERROR_MESSAGE_NULL_PATCH) @Valid PatchTimeEntryRequestDTO patch) {
    public static final String ERROR_MESSAGE_NULL_TIME_ENTRY_ID = "Time entry ID can't be null";
    public static final String ERROR_MESSAGE_NULL_PATCH = "Time entry patch can't be null";
}
