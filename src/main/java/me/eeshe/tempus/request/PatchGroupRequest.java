package me.eeshe.tempus.request;

import java.util.List;

import org.openapitools.jackson.nullable.JsonNullable;

import me.eeshe.tempus.entity.User;

public record PatchGroupRequest(
        String name,
        JsonNullable<List<User>> users) {
}
