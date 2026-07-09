package me.eeshe.tempus.request;

public record UpdateTimeEntryRequest(
        String groupId,
        String userId,
        String projectId,
        String taskId,
        String description,
        boolean isBillable) {
}
