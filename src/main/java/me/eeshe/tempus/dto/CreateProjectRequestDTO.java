package me.eeshe.tempus.dto;

import java.math.BigDecimal;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PositiveOrZero;

public record CreateProjectRequestDTO(
        @NotBlank(message = ERROR_MESSAGE_EMPTY_NAME) String name,
        Long clientId,
        @PositiveOrZero(message = ERROR_MESSAGE_NEGATIVE_RATE) BigDecimal hourlyRate) {
    private static final String ERROR_MESSAGE_EMPTY_NAME = "Project name can't be null or empty";
    private static final String ERROR_MESSAGE_NEGATIVE_RATE = "Project hourly rate can't be negative";
}
