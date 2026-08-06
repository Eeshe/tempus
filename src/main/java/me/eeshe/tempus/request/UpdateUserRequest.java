package me.eeshe.tempus.request;

import java.util.List;

import me.eeshe.tempus.entity.Group;

public record UpdateUserRequest(
        String name,
        String password,
        List<Group> groups) {
}
