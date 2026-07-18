package me.eeshe.tempus.dto;

import java.time.LocalDateTime;

public record ProjectDTO(
        long id,
        String name,
        long userId,
        boolean isPrivate,
        Long clientId,
        LocalDateTime createdAt) {
}
