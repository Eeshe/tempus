package me.eeshe.tempus.request;

import me.eeshe.tempus.entity.Client;
import me.eeshe.tempus.entity.User;

public record CreateProjectRequest(
        String name,
        User user,
        boolean isPrivate,
        Client client) {
}
