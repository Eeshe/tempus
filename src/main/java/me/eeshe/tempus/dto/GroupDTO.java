package me.eeshe.tempus.dto;

import java.time.LocalDateTime;
import java.util.List;

public record GroupDTO(
        long id,
        String name,
        List<Long> userIds,
        LocalDateTime creationTime) {
}
