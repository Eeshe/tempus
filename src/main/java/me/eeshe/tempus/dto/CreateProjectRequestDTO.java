package me.eeshe.tempus.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CreateProjectRequestDTO(
        @NotBlank(message = ERROR_MESSAGE_EMPTY_NAME) String name,
        @NotNull(message = ERROR_MESSAGE_EMPTY_USER) Long userId,
        @NotNull boolean isPrivate,
        Long clientId) {
    private static final String ERROR_MESSAGE_EMPTY_NAME = "Project name can't be null or empty";
    private static final String ERROR_MESSAGE_EMPTY_USER = "Project user can't be null or empty";
}
