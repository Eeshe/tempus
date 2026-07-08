package me.eeshe.tempus.entity.dto;

public record TimeEntryDTO(
        long id,
        String groupId,
        String userId,
        String projectId,
        String taskId,
        String description,
        boolean isBillable) {
}
