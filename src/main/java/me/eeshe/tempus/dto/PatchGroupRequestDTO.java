package me.eeshe.tempus.dto;

import java.util.List;
import java.util.Optional;

import me.eeshe.tempus.common.validation.annotation.NotBlankIfPresent;

public record PatchGroupRequestDTO(
        @NotBlankIfPresent(message = ERROR_MESSAGE_EMPTY_NAME) String name,
        Optional<List<Long>> userIds) {
    private static final String ERROR_MESSAGE_EMPTY_NAME = "Group name can't be empty";
}
