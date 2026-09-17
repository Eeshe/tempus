package me.eeshe.tempus.dto;

import me.eeshe.tempus.common.validation.annotation.NotBlankIfPresent;

public record PatchUserRequestDTO(
        @NotBlankIfPresent(message = ERROR_MESSAGE_EMPTY_NAME) String name,
        @NotBlankIfPresent(message = ERROR_MESSAGE_EMPTY_PASSWORD) String password) {
    private static final String ERROR_MESSAGE_EMPTY_NAME = "User name can't be empty if it's provided";
    private static final String ERROR_MESSAGE_EMPTY_PASSWORD = "User password can't be empty if it's provided";
}
