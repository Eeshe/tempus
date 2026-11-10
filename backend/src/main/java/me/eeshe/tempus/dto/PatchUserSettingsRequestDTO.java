package me.eeshe.tempus.dto;

import java.util.Currency;

import org.openapitools.jackson.nullable.JsonNullable;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

public record PatchUserSettingsRequestDTO(
        @NotNull(message = ERROR_MESSAGE_NULL_THEME) JsonNullable<@Valid ThemePatchDTO> theme,
        @NotNull(message = ERROR_MESSAGE_NULL_CURRENCY) JsonNullable<Currency> currency) {
    public static final String ERROR_MESSAGE_NULL_THEME = "Theme can't be null if provided";
    public static final String ERROR_MESSAGE_NULL_CURRENCY = "Currency can't be null if provided";
    public static final String ERROR_MESSAGE_INVALID_CURRENCY = "Currency must be a valid ISO 4217 currency code";
}
