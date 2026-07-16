package me.eeshe.tempus.request;

import java.util.List;
import java.util.Optional;

import me.eeshe.tempus.entity.User;

public record PatchGroupRequest(
        String name,
        Optional<List<User>> users) {
}
