package me.eeshe.tempus.request;

import java.util.Optional;

import me.eeshe.tempus.entity.User;

public record CreateClientRequest(
        String name,
        User user,
        Optional<Double> hourlyRate) {
}
