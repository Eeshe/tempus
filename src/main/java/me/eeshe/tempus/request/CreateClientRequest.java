package me.eeshe.tempus.request;

import me.eeshe.tempus.entity.User;

public record CreateClientRequest(
        String name,
        User user) {
}
