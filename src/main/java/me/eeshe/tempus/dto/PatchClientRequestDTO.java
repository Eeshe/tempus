package me.eeshe.tempus.dto;

import java.util.Optional;

import jakarta.validation.constraints.DecimalMin;
import me.eeshe.tempus.common.validation.annotation.NotBlankIfPresent;

public record PatchClientRequestDTO(
        @NotBlankIfPresent(message = ERROR_MESSAGE_EMPTY_NAME) String name,
        Optional<@DecimalMin(value = "0", message = ERROR_MESSAGE_NEGATIVE_RATE) Double> hourlyRate) {
    private static final String ERROR_MESSAGE_EMPTY_NAME = "Client name can't be null or empty";
    private static final String ERROR_MESSAGE_NEGATIVE_RATE = "Client hourly rate can't be negative";
}
