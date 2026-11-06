package me.eeshe.tempus.dto;

import org.openapitools.jackson.nullable.JsonNullable;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

public record ThemePatchDTO(
        @NotNull(message = ERROR_MESSAGE_NULL_BACKGROUND)
        @Pattern(regexp = HEX_COLOR, message = ERROR_MESSAGE_INVALID_BACKGROUND)
        JsonNullable<String> background,
        @NotNull(message = ERROR_MESSAGE_NULL_SURFACE)
        @Pattern(regexp = HEX_COLOR, message = ERROR_MESSAGE_INVALID_SURFACE)
        JsonNullable<String> surface,
        @NotNull(message = ERROR_MESSAGE_NULL_TEXT)
        @Pattern(regexp = HEX_COLOR, message = ERROR_MESSAGE_INVALID_TEXT)
        JsonNullable<String> text,
        @NotNull(message = ERROR_MESSAGE_NULL_ACCENT)
        @Pattern(regexp = HEX_COLOR, message = ERROR_MESSAGE_INVALID_ACCENT)
        JsonNullable<String> accent,
        @NotNull(message = ERROR_MESSAGE_NULL_DANGER)
        @Pattern(regexp = HEX_COLOR, message = ERROR_MESSAGE_INVALID_DANGER)
        JsonNullable<String> danger) {

    public static final String HEX_COLOR = "^#[0-9a-fA-F]{6}$";

    public static final String ERROR_MESSAGE_NULL_BACKGROUND = "Theme background color can't be null if provided";
    public static final String ERROR_MESSAGE_INVALID_BACKGROUND = "Theme background color must be a HEX color in #RRGGBB format";
    public static final String ERROR_MESSAGE_NULL_SURFACE = "Theme surface color can't be null if provided";
    public static final String ERROR_MESSAGE_INVALID_SURFACE = "Theme surface color must be a HEX color in #RRGGBB format";
    public static final String ERROR_MESSAGE_NULL_TEXT = "Theme text color can't be null if provided";
    public static final String ERROR_MESSAGE_INVALID_TEXT = "Theme text color must be a HEX color in #RRGGBB format";
    public static final String ERROR_MESSAGE_NULL_ACCENT = "Theme accent color can't be null if provided";
    public static final String ERROR_MESSAGE_INVALID_ACCENT = "Theme accent color must be a HEX color in #RRGGBB format";
    public static final String ERROR_MESSAGE_NULL_DANGER = "Theme danger color can't be null if provided";
    public static final String ERROR_MESSAGE_INVALID_DANGER = "Theme danger color must be a HEX color in #RRGGBB format";
}
