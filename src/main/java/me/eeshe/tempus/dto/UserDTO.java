package me.eeshe.tempus.dto;

import java.time.Instant;
import java.util.List;

public record UserDTO(
        long id,
        String name,
        List<Long> groupIds,
        Instant createdAt) {
}
