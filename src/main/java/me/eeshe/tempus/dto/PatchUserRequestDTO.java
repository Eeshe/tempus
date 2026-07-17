package me.eeshe.tempus.dto;

import java.util.List;
import java.util.Optional;

import me.eeshe.tempus.common.validation.annotation.NotBlankIfPresent;

public record PatchUserRequestDTO(
        @NotBlankIfPresent(message = ERROR_MESSAGE_EMPTY_NAME) String name,
        Optional<List<Long>> groupIds) {
    private static final String ERROR_MESSAGE_EMPTY_NAME = "User name can't be empty if it's provided";
}
