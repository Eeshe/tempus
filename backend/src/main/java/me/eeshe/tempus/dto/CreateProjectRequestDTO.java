package me.eeshe.tempus.dto;

import java.math.BigDecimal;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

public record CreateProjectRequestDTO(
        @NotBlank(message = ERROR_MESSAGE_EMPTY_NAME) String name,
        Long clientId,
        @PositiveOrZero(message = ERROR_MESSAGE_NEGATIVE_RATE) BigDecimal hourlyRate,
        @Size(min = 2, max = 7, message = ERROR_MESSAGE_INVALID_COLOR_LENGTH) @Pattern(regexp = "^#[0-9A-Fa-f]*$", message = ERROR_MESSAGE_INVALID_COLOR_FORMAT) String color) {
    public static final String ERROR_MESSAGE_EMPTY_NAME = "Project name can't be null or empty";
    public static final String ERROR_MESSAGE_NEGATIVE_RATE = "Project hourly rate can't be negative";
    public static final String ERROR_MESSAGE_INVALID_COLOR_LENGTH = "Project color must start with # and be between 2 and 7 characters long";
    public static final String ERROR_MESSAGE_INVALID_COLOR_FORMAT = "Project color must be a valid HEX color";
}
