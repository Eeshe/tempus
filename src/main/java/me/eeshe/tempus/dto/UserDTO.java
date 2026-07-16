package me.eeshe.tempus.dto;

import java.time.LocalDateTime;
import java.util.List;

public record UserDTO(
        long id,
        String name,
        List<Long> groupIds,
        LocalDateTime createdAt) {
}
