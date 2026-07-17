package me.eeshe.tempus.request;

import java.util.Optional;

import me.eeshe.tempus.entity.Client;

public record PatchProjectRequest(
        String name,
        Optional<Boolean> isPrivate,
        Optional<Client> client) {
}
