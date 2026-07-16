package me.eeshe.tempus.request;

import java.util.List;

import me.eeshe.tempus.entity.User;

public record UpdateGroupRequest(
        String name,
        List<User> users) {
}
