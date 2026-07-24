package me.eeshe.tempus.request;

import org.openapitools.jackson.nullable.JsonNullable;

import me.eeshe.tempus.entity.Group;
import me.eeshe.tempus.entity.Project;
import me.eeshe.tempus.entity.Task;

public record PatchTimeEntryRequest(
        JsonNullable<Group> group,
        JsonNullable<Project> project,
        JsonNullable<Task> task,
        JsonNullable<String> description,
        JsonNullable<Boolean> isBillable) {
}
