package me.eeshe.tempus.dto;

import java.util.List;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

public record DeleteTimeEntriesRequestDTO(
        @NotEmpty(message = ERROR_MESSAGE_EMPTY_TIME_ENTRY_IDS) List<@NotNull(message = ERROR_MESSAGE_NULL_TIME_ENTRY_ID) Long> timeEntryIds) {
    public static final String ERROR_MESSAGE_EMPTY_TIME_ENTRY_IDS = "Time entry IDs can't be empty";
    public static final String ERROR_MESSAGE_NULL_TIME_ENTRY_ID = "Time entry ID can't be null";
}
