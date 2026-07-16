package me.eeshe.tempus.dto;

import java.util.List;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

public record UpdateUserRequestDTO(
        @NotEmpty(message = ERROR_MESSAGE_EMPTY_NAME) String name,
        @NotNull(message = ERROR_MESSAGE_EMPTY_GROUPS) List<Long> groupIds) {
    private static final String ERROR_MESSAGE_EMPTY_NAME = "User name can't be null or empty";
    private static final String ERROR_MESSAGE_EMPTY_GROUPS = "User groups can't be null";
}
