package me.eeshe.tempus.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

public record UpdateGroupRequestDTO(
        @NotEmpty(message = ERROR_MESSAGE_EMPTY_NAME) @NotNull(message = ERROR_MESSAGE_EMPTY_NAME) String name) {
    private static final String ERROR_MESSAGE_EMPTY_NAME = "Group name can't be null nor empty";
}
