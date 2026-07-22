package me.eeshe.tempus.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

public record CreateClientRequestDTO(
        @NotBlank(message = ERROR_MESSAGE_EMPTY_NAME) String name,
        @NotNull(message = ERROR_MESSAGE_EMPTY_USER) Long userId,
        @PositiveOrZero(message = ERROR_MESSAGE_NEGATIVE_RATE) Double hourlyRate) {
    private static final String ERROR_MESSAGE_EMPTY_NAME = "Client name can't be null or empty";
    private static final String ERROR_MESSAGE_EMPTY_USER = "Client user ID can't be null or empty";
    private static final String ERROR_MESSAGE_NEGATIVE_RATE = "Client hourly rate can't be negative";
}
