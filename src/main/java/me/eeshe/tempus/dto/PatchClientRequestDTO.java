package me.eeshe.tempus.dto;

import org.openapitools.jackson.nullable.JsonNullable;

import jakarta.validation.constraints.PositiveOrZero;
import me.eeshe.tempus.common.validation.annotation.NotBlankIfPresent;

public record PatchClientRequestDTO(
        @NotBlankIfPresent(message = ERROR_MESSAGE_EMPTY_NAME) String name,
        @PositiveOrZero(message = ERROR_MESSAGE_NEGATIVE_RATE) JsonNullable<Double> hourlyRate) {
    private static final String ERROR_MESSAGE_EMPTY_NAME = "Client name can't be null or empty";
    private static final String ERROR_MESSAGE_NEGATIVE_RATE = "Client hourly rate can't be negative";
}
