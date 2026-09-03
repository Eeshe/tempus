package me.eeshe.tempus.model;

import java.util.List;

import me.eeshe.tempus.entity.TimeEntry;

public class Report {
    private final List<TimeEntry> timeEntries;
    private final long totalTrackedTimeMillis;

    public Report(List<TimeEntry> timeEntries, long totalTrackedTimeMillis) {
        this.timeEntries = timeEntries;
        this.totalTrackedTimeMillis = totalTrackedTimeMillis;
    }

    public List<TimeEntry> getTimeEntries() {
        return timeEntries;
    }

    public long getTotalTrackedTimeMillis() {
        return totalTrackedTimeMillis;
    }
}
