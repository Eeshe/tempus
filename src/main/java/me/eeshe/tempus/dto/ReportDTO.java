package me.eeshe.tempus.dto;

import java.util.List;

public record ReportDTO<T>(
        long totalTrackedTimeMillis,
        List<T> reportEntries) {
}
