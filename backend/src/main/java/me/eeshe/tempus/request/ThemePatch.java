package me.eeshe.tempus.request;

import org.openapitools.jackson.nullable.JsonNullable;

public record ThemePatch(
        JsonNullable<String> background,
        JsonNullable<String> surface,
        JsonNullable<String> text,
        JsonNullable<String> accent,
        JsonNullable<String> danger) {
}
