package me.eeshe.tempus.service.impl;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import org.springframework.stereotype.Service;

import me.eeshe.tempus.entity.TimeEntry;
import me.eeshe.tempus.model.ClientReportEntry;
import me.eeshe.tempus.model.ProjectReportEntry;
import me.eeshe.tempus.model.Report;
import me.eeshe.tempus.repository.TimeEntryRepository;
import me.eeshe.tempus.request.ReportRequest;
import me.eeshe.tempus.service.ReportService;
import me.eeshe.tempus.specification.TimeEntrySpecification;

@Service
public class ReportServiceImpl implements ReportService {
    private final TimeEntryRepository timeEntryRepository;

    public ReportServiceImpl(TimeEntryRepository timeEntryRepository) {
        this.timeEntryRepository = timeEntryRepository;
    }

    @Override
    public Report<ProjectReportEntry> generateProjectReport(ReportRequest reportRequest) {
        return generateReport(
                reportRequest,
                (timeEntry) -> timeEntry.getProject(),
                (entry) -> new ProjectReportEntry(entry.getKey(), entry.getValue()));
    }

    @Override
    public Report<ClientReportEntry> generateClientReport(ReportRequest reportRequest) {
        return generateReport(
                reportRequest,
                (timeEntry) -> timeEntry.getProject().getClient(),
                (entry) -> new ClientReportEntry(entry.getKey(), entry.getValue()));
    }

    private <T, U> Report<T> generateReport(
            ReportRequest reportRequest,
            Function<TimeEntry, U> mapKeyFunction,
            Function<Map.Entry<U, Long>, T> reportEntryCreateFunction) {
        final List<TimeEntry> timeEntries = timeEntryRepository.findAll(TimeEntrySpecification.withFilters(
                reportRequest.userId(),
                reportRequest.startDate(),
                reportRequest.endDate(),
                reportRequest.projectIds(),
                reportRequest.taskIds(),
                reportRequest.clientIds(),
                reportRequest.descriptions(),
                reportRequest.isBillable()));

        final Map<U, Long> trackedTimeMillisMap = new HashMap<>();
        long totalBillableTrackedTimeMillis = 0;
        long totalNonBillableTrackedTimeMillis = 0;
        BigDecimal totalAccumulatedPay = BigDecimal.ZERO;
        for (TimeEntry timeEntry : timeEntries) {
            final U reportKey = mapKeyFunction.apply(timeEntry);
            if (reportKey == null) {
                continue;
            }
            final long timeEntryDurationMillis = Duration.between(
                    timeEntry.getStartTime(),
                    timeEntry.getEndTime()).toMillis();
            trackedTimeMillisMap.compute(reportKey, (_, trackedTimeMillis) -> {
                if (trackedTimeMillis == null) {
                    return timeEntryDurationMillis;
                }
                return trackedTimeMillis + timeEntryDurationMillis;
            });
            if (timeEntry.isBillable()) {
                totalBillableTrackedTimeMillis += timeEntryDurationMillis;

                final BigDecimal timeEntryHourlyRate = timeEntry.getProject().getHourlyRate();
                if (timeEntryHourlyRate != null) {
                    totalAccumulatedPay = totalAccumulatedPay.add(timeEntryHourlyRate);
                }
            } else {
                totalNonBillableTrackedTimeMillis += timeEntryDurationMillis;
            }
        }
        return new Report<>(
                totalBillableTrackedTimeMillis,
                totalNonBillableTrackedTimeMillis,
                totalAccumulatedPay,
                trackedTimeMillisMap.entrySet().stream()
                        .map(reportEntryCreateFunction).toList());
    }
}
