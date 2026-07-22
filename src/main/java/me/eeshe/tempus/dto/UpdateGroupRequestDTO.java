package me.eeshe.tempus.dto;

import java.util.List;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record UpdateGroupRequestDTO(
        @NotBlank(message = ERROR_MESSAGE_EMPTY_NAME) String name,
        @NotNull(message = ERROR_MESSAGE_EMPTY_USERS) List<Long> userIds) {
    private static final String ERROR_MESSAGE_EMPTY_NAME = "Group name can't be null or empty";
    private static final String ERROR_MESSAGE_EMPTY_USERS = "Group users can't be null";
}
