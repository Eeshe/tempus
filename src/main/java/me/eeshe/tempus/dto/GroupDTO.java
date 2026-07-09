package me.eeshe.tempus.dto;

import java.time.LocalDateTime;

public record GroupDTO(
        long id,
        String name,
        LocalDateTime creationTime) {
}
