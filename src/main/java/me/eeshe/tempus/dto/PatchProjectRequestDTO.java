package me.eeshe.tempus.dto;

import java.math.BigDecimal;

import org.openapitools.jackson.nullable.JsonNullable;

import jakarta.validation.constraints.PositiveOrZero;
import me.eeshe.tempus.common.validation.annotation.NotBlankIfPresent;

public record PatchProjectRequestDTO(
        @NotBlankIfPresent(message = ERROR_MESSAGE_EMPTY_NAME) String name,
        JsonNullable<Boolean> isPrivate,
        @PositiveOrZero(message = ERROR_MESSAGE_NEGATIVE_RATE) JsonNullable<BigDecimal> hourlyRate,
        JsonNullable<Long> clientId) {
    private static final String ERROR_MESSAGE_EMPTY_NAME = "Project name can't be empty if provided";
    private static final String ERROR_MESSAGE_NEGATIVE_RATE = "Project hourly rate can't be negative";
}
