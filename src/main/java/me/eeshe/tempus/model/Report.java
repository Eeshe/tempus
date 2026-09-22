package me.eeshe.tempus.model;

import java.math.BigDecimal;
import java.util.List;

public class Report<T> {
    private final long totalTrackedTimeMillis;
    private final long totalBillableTrackedTimeMillis;
    private final long totalNonBillableTrackedTimeMillis;
    private final BigDecimal totalAccumulatedPay;
    private final List<T> reportEntries;

    public Report(
            long totalBillableTrackedTimeMillis,
            long totalNonBillableTrackedTimeMillis,
            BigDecimal totalAccumulatedPay,
            List<T> reportEntries) {
        this.totalTrackedTimeMillis = totalNonBillableTrackedTimeMillis + totalBillableTrackedTimeMillis;
        this.totalBillableTrackedTimeMillis = totalBillableTrackedTimeMillis;
        this.totalNonBillableTrackedTimeMillis = totalNonBillableTrackedTimeMillis;
        this.totalAccumulatedPay = totalAccumulatedPay;
        this.reportEntries = reportEntries;
    }

    public long getTotalTrackedTimeMillis() {
        return totalTrackedTimeMillis;
    }

    public long getTotalBillableTrackedTimeMillis() {
        return totalBillableTrackedTimeMillis;
    }

    public long getTotalNonBillableTrackedTimeMillis() {
        return totalNonBillableTrackedTimeMillis;
    }

    public BigDecimal getTotalAccumulatedPay() {
        return totalAccumulatedPay;
    }

    public List<T> getReportEntries() {
        return reportEntries;
    }
}
