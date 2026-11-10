package me.eeshe.tempus.request;

import java.util.Currency;

import org.openapitools.jackson.nullable.JsonNullable;

public record PatchUserSettingsRequest(ThemePatch theme, JsonNullable<Currency> currency) {
}
