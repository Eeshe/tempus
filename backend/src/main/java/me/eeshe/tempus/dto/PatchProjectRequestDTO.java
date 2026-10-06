package me.eeshe.tempus.dto;

import java.math.BigDecimal;

import org.openapitools.jackson.nullable.JsonNullable;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import me.eeshe.tempus.common.validation.annotation.NotBlankIfPresent;

public record PatchProjectRequestDTO(
        @NotBlankIfPresent(message = ERROR_MESSAGE_EMPTY_NAME) String name,
        @PositiveOrZero(message = ERROR_MESSAGE_NEGATIVE_RATE) JsonNullable<BigDecimal> hourlyRate,
        JsonNullable<Long> clientId,
        @NotNull(message = ERROR_MESSAGE_NULL_ARCHIVED) JsonNullable<Boolean> isArchived) {
    public static final String ERROR_MESSAGE_EMPTY_NAME = "Project name can't be empty if provided";
    public static final String ERROR_MESSAGE_NEGATIVE_RATE = "Project hourly rate can't be negative";
    public static final String ERROR_MESSAGE_NULL_ARCHIVED = "Project archived status can't be null if provided";
}
