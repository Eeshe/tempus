package me.eeshe.tempus.dto;

import jakarta.validation.constraints.NotBlank;

public record CreateUserRequestDTO(
        @NotBlank(message = ERROR_MESSAGE_EMPTY_NAME) String name) {
    private static final String ERROR_MESSAGE_EMPTY_NAME = "User name can't be null nor empty";
}
