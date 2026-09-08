package me.eeshe.tempus.dto;

import java.math.BigDecimal;
import java.time.Instant;

public record ProjectDTO(
        long id,
        String name,
        long userId,
        boolean isPrivate,
        BigDecimal hourlyRate,
        Long clientId,
        Instant createdAt) {
}
