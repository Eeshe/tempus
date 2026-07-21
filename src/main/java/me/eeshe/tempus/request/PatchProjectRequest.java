package me.eeshe.tempus.request;

import org.openapitools.jackson.nullable.JsonNullable;

import me.eeshe.tempus.entity.Client;

public record PatchProjectRequest(
        String name,
        JsonNullable<Boolean> isPrivate,
        JsonNullable<Client> client) {
}
