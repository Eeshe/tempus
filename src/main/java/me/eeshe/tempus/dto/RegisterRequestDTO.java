package me.eeshe.tempus.dto;

import jakarta.validation.constraints.NotBlank;

public record RegisterRequestDTO(
        @NotBlank String username,
        @NotBlank String password) {
    private static final String ERROR_MESSAGE_EMPTY_USERNAME = "Username can't be null or empty";
    private static final String ERROR_MESSAGE_EMPTY_PASSWORD = "Password can't be null or empty";
}
