package me.eeshe.tempus.dto;

import jakarta.validation.constraints.NotBlank;

public record CreateUserRequestDTO(
        @NotBlank(message = ERROR_MESSAGE_EMPTY_NAME) String name,
        @NotBlank(message = ERROR_MESSAGE_EMPTY_PASSWORD) String password) {
    private static final String ERROR_MESSAGE_EMPTY_NAME = "User name can't be null nor empty";
    private static final String ERROR_MESSAGE_EMPTY_PASSWORD = "User password can't be null nor empty";
}
