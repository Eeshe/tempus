package me.eeshe.tempus.request;

import java.util.Optional;

public record PatchClientRequest(
        String name,
        Optional<Double> hourlyRate) {
}
