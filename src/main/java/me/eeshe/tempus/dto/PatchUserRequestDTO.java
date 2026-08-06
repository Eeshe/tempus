package me.eeshe.tempus.dto;

import java.util.List;

import org.openapitools.jackson.nullable.JsonNullable;

import jakarta.validation.constraints.NotNull;
import me.eeshe.tempus.common.validation.annotation.NotBlankIfPresent;

public record PatchUserRequestDTO(
        @NotBlankIfPresent(message = ERROR_MESSAGE_EMPTY_NAME) String name,
        @NotBlankIfPresent(message = ERROR_MESSAGE_EMPTY_PASSWORD) String password,
        @NotNull(message = ERROR_MESSAGE_NULL_GROUPS) JsonNullable<List<Long>> groupIds) {
    private static final String ERROR_MESSAGE_EMPTY_NAME = "User name can't be empty if it's provided";
    private static final String ERROR_MESSAGE_EMPTY_PASSWORD = "User password can't be empty if it's provided";
    private static final String ERROR_MESSAGE_NULL_GROUPS = "User groups can't be null";
}
