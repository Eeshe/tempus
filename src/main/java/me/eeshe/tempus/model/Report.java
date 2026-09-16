package me.eeshe.tempus.model;

import java.util.List;

public class Report<T> {
    private final long totalTrackedTimeMillis;
    private final List<T> reportEntries;

    public Report(long totalTrackedTimeMillis, List<T> reportEntries) {
        this.totalTrackedTimeMillis = totalTrackedTimeMillis;
        this.reportEntries = reportEntries;
    }

    public long getTotalTrackedTimeMillis() {
        return totalTrackedTimeMillis;
    }

    public List<T> getReportEntries() {
        return reportEntries;
    }
}
