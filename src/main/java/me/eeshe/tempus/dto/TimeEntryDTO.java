package me.eeshe.tempus.dto;

import java.time.LocalDateTime;

public record TimeEntryDTO(
        long id,
        Long groupId,
        long userId,
        long projectId,
        Long taskId,
        String description,
        boolean isBillable,
        LocalDateTime createdAt) {
}
