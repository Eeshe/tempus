package me.eeshe.tempus.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record ReportDay<T>(
        LocalDate date,
        long totalTrackedTimeMillis,
        long totalBillableTrackedTimeMillis,
        long totalNonBillableTrackedTimeMillis,
        BigDecimal totalAccumulatedPay,
        List<T> reportEntries) {

    public ReportDay(
            LocalDate date,
            long totalBillableTrackedTimeMillis,
            long totalNonBillableTrackedTimeMillis,
            BigDecimal totalAccumulatedPay,
            List<T> reportEntries) {
        this(
                date,
                totalBillableTrackedTimeMillis + totalNonBillableTrackedTimeMillis,
                totalBillableTrackedTimeMillis,
                totalNonBillableTrackedTimeMillis,
                totalAccumulatedPay,
                reportEntries);
    }
}
