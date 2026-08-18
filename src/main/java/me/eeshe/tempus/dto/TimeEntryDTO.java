package me.eeshe.tempus.dto;

import java.time.LocalDateTime;

public record TimeEntryDTO(
        long id,
        Long groupId,
        long userId,
        ProjectDTO project,
        TaskDTO task,
        String description,
        boolean isBillable,
        LocalDateTime startTime,
        LocalDateTime endTime,
        LocalDateTime createdAt) {
}
