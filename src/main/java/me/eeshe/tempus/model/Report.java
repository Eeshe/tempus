package me.eeshe.tempus.model;

import java.math.BigDecimal;
import java.util.List;

public record Report<T>(
        long totalTrackedTimeMillis,
        long totalBillableTrackedTimeMillis,
        long totalNonBillableTrackedTimeMillis,
        BigDecimal totalAccumulatedPay,
        List<T> reportEntries) {

    public Report(
            long totalBillableTrackedTimeMillis,
            long totalNonBillableTrackedTimeMillis,
            BigDecimal totalAccumulatedPay,
            List<T> reportEntries) {
        this(
                totalBillableTrackedTimeMillis + totalNonBillableTrackedTimeMillis,
                totalBillableTrackedTimeMillis,
                totalNonBillableTrackedTimeMillis,
                totalAccumulatedPay,
                reportEntries);
    }
}
