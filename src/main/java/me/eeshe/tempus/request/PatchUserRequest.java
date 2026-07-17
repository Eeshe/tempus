package me.eeshe.tempus.request;

import java.util.List;
import java.util.Optional;

import me.eeshe.tempus.entity.Group;

public record PatchUserRequest(
        String name,
        Optional<List<Group>> groups) {
}
