package me.eeshe.tempus.dto;

import java.time.Instant;

public record UserDTO(
        long id,
        String name,
        Instant createdAt) {
}
