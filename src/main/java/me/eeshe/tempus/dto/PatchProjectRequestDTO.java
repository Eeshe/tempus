package me.eeshe.tempus.dto;

import org.openapitools.jackson.nullable.JsonNullable;

import me.eeshe.tempus.common.validation.annotation.NotBlankIfPresent;

public record PatchProjectRequestDTO(
        @NotBlankIfPresent(message = ERROR_MESSAGE_EMPTY_NAME) String name,
        JsonNullable<Boolean> isPrivate,
        JsonNullable<Long> clientId) {
    private static final String ERROR_MESSAGE_EMPTY_NAME = "Project name can't be empty if provided";
}
