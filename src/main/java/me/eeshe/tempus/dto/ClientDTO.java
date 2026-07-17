package me.eeshe.tempus.dto;

import java.time.LocalDateTime;

public record ClientDTO(
        long id,
        String name,
        long userId,
        Double hourlyRate,
        LocalDateTime createdAt) {
}
