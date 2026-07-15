package me.eeshe.tempus.dto;

import java.time.LocalDateTime;

public record UserDTO(
        long id,
        String name,
        LocalDateTime createdAt) {
}
