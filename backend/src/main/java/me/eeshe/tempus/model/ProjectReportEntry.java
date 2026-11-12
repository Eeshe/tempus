package me.eeshe.tempus.model;

import java.util.List;

import me.eeshe.tempus.entity.Project;
import me.eeshe.tempus.entity.TimeEntry;

public record ProjectReportEntry(
        Project project,
        long trackedTimeMillis,
        List<TimeEntry> timeEntries) {
}
