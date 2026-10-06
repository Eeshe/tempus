package me.eeshe.tempus.model;

import me.eeshe.tempus.entity.Project;

public record ProjectReportEntry(
        Project project,
        long trackedTimeMillis) {
}
