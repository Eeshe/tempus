package me.eeshe.tempus.model;

import java.math.BigDecimal;
import java.util.List;

public record Report<T>(
        long totalTrackedTimeMillis,
        long totalBillableTrackedTimeMillis,
        long totalNonBillableTrackedTimeMillis,
        BigDecimal totalAccumulatedPay,
        List<ReportDay<T>> reportDays) {

    public Report(
            long totalBillableTrackedTimeMillis,
            long totalNonBillableTrackedTimeMillis,
            BigDecimal totalAccumulatedPay,
            List<ReportDay<T>> reportDays) {
        this(
                totalBillableTrackedTimeMillis + totalNonBillableTrackedTimeMillis,
                totalBillableTrackedTimeMillis,
                totalNonBillableTrackedTimeMillis,
                totalAccumulatedPay,
                reportDays);
    }
}
