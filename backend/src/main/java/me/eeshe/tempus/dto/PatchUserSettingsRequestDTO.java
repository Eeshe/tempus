package me.eeshe.tempus.dto;

import org.openapitools.jackson.nullable.JsonNullable;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

public record PatchUserSettingsRequestDTO(
        @NotNull(message = ERROR_MESSAGE_NULL_THEME) JsonNullable<@Valid ThemePatchDTO> theme) {
    public static final String ERROR_MESSAGE_NULL_THEME = "Theme can't be null if provided";
}
