package me.eeshe.tempus.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CreateTaskRequestDTO(
        @NotBlank(message = ERROR_MESSAGE_EMPTY_NAME) String name,
        @NotNull(message = ERROR_MESSAGE_EMPTY_PROJECT) Long projectId) {
    private static final String ERROR_MESSAGE_EMPTY_NAME = "Task name can't be null or empty";
    private static final String ERROR_MESSAGE_EMPTY_PROJECT = "Task project can't be null";
}
