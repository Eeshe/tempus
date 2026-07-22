package me.eeshe.tempus.dto;

import java.time.LocalDateTime;

public record TaskDTO(
        long id,
        String name,
        long userId,
        long projectId,
        LocalDateTime createdAt) {
}
