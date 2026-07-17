package me.eeshe.tempus.dto;

import java.util.Optional;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

public record CreateClientRequestDTO(
        @NotEmpty(message = ERROR_MESSAGE_EMPTY_NAME) String name,
        @NotNull(message = ERROR_MESSAGE_EMPTY_USER) Long userId,
        Optional<@DecimalMin(value = "0", message = ERROR_MESSAGE_NEGATIVE_RATE) Double> hourlyRate) {
    private static final String ERROR_MESSAGE_EMPTY_NAME = "Client name can't be null or empty";
    private static final String ERROR_MESSAGE_EMPTY_USER = "Client user ID can't be null or empty";
    private static final String ERROR_MESSAGE_NEGATIVE_RATE = "Client hourly rate can't be negative";
}
