package me.eeshe.tempus.request;

import java.util.Optional;

import me.eeshe.tempus.entity.Client;
import me.eeshe.tempus.entity.User;

public record CreateProjectRequest(
        String name,
        User user,
        boolean isPrivate,
        Optional<Client> client) {
}
