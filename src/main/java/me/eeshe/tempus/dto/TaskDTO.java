package me.eeshe.tempus.dto;

import java.time.LocalDateTime;

public record TaskDTO(
        long id,
        String name,
        long userId,
        ProjectDTO project,
        LocalDateTime createdAt) {
}
