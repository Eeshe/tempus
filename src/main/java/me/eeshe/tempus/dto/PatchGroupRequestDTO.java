package me.eeshe.tempus.dto;

import java.util.List;

import org.openapitools.jackson.nullable.JsonNullable;

import jakarta.validation.constraints.NotNull;
import me.eeshe.tempus.common.validation.annotation.NotBlankIfPresent;

public record PatchGroupRequestDTO(
        @NotBlankIfPresent(message = ERROR_MESSAGE_EMPTY_NAME) String name,
        @NotNull(message = ERROR_MESSAGE_NULL_USERS) JsonNullable<List<Long>> userIds) {
    private static final String ERROR_MESSAGE_EMPTY_NAME = "Group name can't be empty if it's provided";
    private static final String ERROR_MESSAGE_NULL_USERS = "Group users can't be null";
}
