package me.eeshe.tempus.model;

import java.util.List;

public class Report {
    private final long totalTrackedTimeMillis;
    private final List<ProjectReportEntry> projectReportEntries;

    public Report(long totalTrackedTimeMillis, List<ProjectReportEntry> projectReportEntries) {
        this.totalTrackedTimeMillis = totalTrackedTimeMillis;
        this.projectReportEntries = projectReportEntries;
    }

    public long getTotalTrackedTimeMillis() {
        return totalTrackedTimeMillis;
    }

    public List<ProjectReportEntry> getProjectReportEntries() {
        return projectReportEntries;
    }
}
