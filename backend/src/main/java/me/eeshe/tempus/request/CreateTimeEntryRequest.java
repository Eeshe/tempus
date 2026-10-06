package me.eeshe.tempus.request;

import java.time.Instant;

import me.eeshe.tempus.entity.Project;
import me.eeshe.tempus.entity.Task;
import me.eeshe.tempus.entity.User;

public record CreateTimeEntryRequest(
        User user,
        Project project,
        Task task,
        String description,
        boolean isBillable,
        Instant startTime,
        Instant endTime) {
}
