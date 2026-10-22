package me.eeshe.tempus.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record ReportDayDTO<T>(
        LocalDate date,
        long totalTrackedTimeMillis,
        long totalBillableTrackedTimeMillis,
        long totalNonBillableTrackedTimeMillis,
        BigDecimal totalAccumulatedPay,
        List<T> reportEntries) {
}
