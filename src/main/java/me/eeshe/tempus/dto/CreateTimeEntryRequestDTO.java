package me.eeshe.tempus.dto;

public record CreateTimeEntryRequestDTO(
        long groupId,
        long userId,
        long projectId,
        long taskId,
        String description,
        boolean isBillable) {
}
