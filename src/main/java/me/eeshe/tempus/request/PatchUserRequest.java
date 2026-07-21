package me.eeshe.tempus.request;

import java.util.List;

import org.openapitools.jackson.nullable.JsonNullable;

import me.eeshe.tempus.entity.Group;

public record PatchUserRequest(
        String name,
        JsonNullable<List<Group>> groups) {
}
