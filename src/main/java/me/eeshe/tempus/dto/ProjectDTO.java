package me.eeshe.tempus.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

public record ProjectDTO(
        long id,
        String name,
        long userId,
        boolean isPrivate,
        BigDecimal hourlyRate,
        List<TaskDTO> tasks,
        Long clientId,
        Instant createdAt) {
}
