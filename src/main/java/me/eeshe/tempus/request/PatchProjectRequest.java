package me.eeshe.tempus.request;

import java.math.BigDecimal;

import org.openapitools.jackson.nullable.JsonNullable;

import me.eeshe.tempus.entity.Client;

public record PatchProjectRequest(
        String name,
        JsonNullable<Boolean> isPrivate,
        JsonNullable<BigDecimal> hourlyRate,
        JsonNullable<Client> client) {
}
