package me.eeshe.tempus.dto;

import java.time.Instant;
import java.util.List;

public record GroupDTO(
        long id,
        String name,
        List<Long> userIds,
        Instant createdAt) {
}
