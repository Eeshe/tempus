package me.eeshe.tempus.dto;

import java.time.Instant;

public record TaskDTO(
        long id,
        String name,
        long userId,
        long projectId,
        Instant createdAt) {
}
