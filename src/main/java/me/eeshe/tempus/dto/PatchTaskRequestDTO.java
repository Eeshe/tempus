package me.eeshe.tempus.dto;

import me.eeshe.tempus.common.validation.annotation.NotBlankIfPresent;

public record PatchTaskRequestDTO(
        @NotBlankIfPresent(message = ERROR_MESSAGE_EMPTY_NAME) String name) {
    private static final String ERROR_MESSAGE_EMPTY_NAME = "Task name can't be empty if provided";
}
