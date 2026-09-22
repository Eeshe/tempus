package me.eeshe.tempus.dto;

import java.math.BigDecimal;
import java.util.List;

public record ReportDTO<T>(
        long totalTrackedTimeMillis,
        long totalBillableTrackedTimeMillis,
        long totalNonBillableTrackedTimeMillis,
        BigDecimal totalAccumulatedPay,
        List<T> reportEntries) {
}
