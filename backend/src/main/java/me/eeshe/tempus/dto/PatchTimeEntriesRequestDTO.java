package me.eeshe.tempus.dto;

import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;

public record PatchTimeEntriesRequestDTO(
        @NotEmpty(message = ERROR_MESSAGE_EMPTY_TIME_ENTRIES) List<@Valid TimeEntryPatchDTO> timeEntries) {
    public static final String ERROR_MESSAGE_EMPTY_TIME_ENTRIES = "Time entries can't be empty";
}
