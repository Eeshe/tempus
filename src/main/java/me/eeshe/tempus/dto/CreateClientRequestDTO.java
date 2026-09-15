package me.eeshe.tempus.dto;

import jakarta.validation.constraints.NotBlank;

public record CreateClientRequestDTO(
        @NotBlank(message = ERROR_MESSAGE_EMPTY_NAME) String name) {
    private static final String ERROR_MESSAGE_EMPTY_NAME = "Client name can't be null or empty";
}
