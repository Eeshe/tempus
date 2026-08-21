package me.eeshe.tempus.dto;

import java.time.Instant;

public record ProjectDTO(
        long id,
        String name,
        long userId,
        boolean isPrivate,
        Long clientId,
        Instant createdAt) {
}
