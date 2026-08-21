package me.eeshe.tempus.dto;

import java.time.Instant;

public record TimeEntryDTO(
        long id,
        Long groupId,
        long userId,
        ProjectDTO project,
        TaskDTO task,
        String description,
        boolean isBillable,
        Instant startTime,
        Instant endTime,
        Instant createdAt) {
}
