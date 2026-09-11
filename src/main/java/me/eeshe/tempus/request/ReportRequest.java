package me.eeshe.tempus.request;

import java.time.LocalDate;
import java.util.List;

public record ReportRequest(
        LocalDate startDate,
        LocalDate endDate,
        List<Long> projectIds,
        List<Long> taskIds,
        List<String> descriptions,
        Boolean isBillable) {

    public static ReportRequest fromProjectId(long projectId) {
        return new ReportRequest(null, null, List.of(projectId), null, null, null);
    }
}
