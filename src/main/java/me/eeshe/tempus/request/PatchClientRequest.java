package me.eeshe.tempus.request;

import org.openapitools.jackson.nullable.JsonNullable;

public record PatchClientRequest(
        String name,
        JsonNullable<Double> hourlyRate) {
}
