package me.eeshe.tempus.dto;

import java.time.Instant;

public record ClientDTO(
        long id,
        String name,
        long userId,
        Double hourlyRate,
        Instant createdAt) {
}
