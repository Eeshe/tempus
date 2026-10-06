package me.eeshe.tempus.dto;

import me.eeshe.tempus.common.validation.annotation.NotBlankIfPresent;

public record PatchClientRequestDTO(
        @NotBlankIfPresent(message = ERROR_MESSAGE_EMPTY_NAME) String name) {
    public static final String ERROR_MESSAGE_EMPTY_NAME = "Client name can't be null or empty";
}
