package me.eeshe.tempus.request;

import me.eeshe.tempus.entity.Project;
import me.eeshe.tempus.entity.User;

public record CreateTaskRequest(
        String name,
        User user,
        Project project) {
}
