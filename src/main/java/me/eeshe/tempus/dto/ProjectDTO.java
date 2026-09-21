package me.eeshe.tempus.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

public record ProjectDTO(
        long id,
        String name,
        long userId,
        BigDecimal hourlyRate,
        List<TaskDTO> tasks,
        ClientDTO client,
        Instant createdAt) {
}
