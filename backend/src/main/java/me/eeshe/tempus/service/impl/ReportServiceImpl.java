package me.eeshe.tempus.service.impl;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.function.Function;

import org.springframework.stereotype.Service;

import me.eeshe.tempus.entity.TimeEntry;
import me.eeshe.tempus.model.ClientReportEntry;
import me.eeshe.tempus.model.ProjectReportEntry;
import me.eeshe.tempus.model.Report;
import me.eeshe.tempus.model.ReportDay;
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

        final ZoneId zoneId = reportRequest.zoneId();
        final Map<LocalDate, ReportDayAccumulator<U>> reportDayAccumulators = new TreeMap<>();
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
            final LocalDate reportDate = timeEntry.getStartTime().atZone(zoneId).toLocalDate();
            final ReportDayAccumulator<U> reportDayAccumulator = reportDayAccumulators.computeIfAbsent(
                    reportDate,
                    (_) -> new ReportDayAccumulator<>());
            reportDayAccumulator.addTrackedTimeMillis(reportKey, timeEntryDurationMillis);

            if (timeEntry.isBillable()) {
                totalBillableTrackedTimeMillis += timeEntryDurationMillis;
                reportDayAccumulator.addBillableTrackedTimeMillis(timeEntryDurationMillis);

                final BigDecimal timeEntryHourlyRate = timeEntry.getProject().getHourlyRate();
                if (timeEntryHourlyRate != null) {
                    final BigDecimal timeEntryDurationHours = BigDecimal.valueOf(timeEntryDurationMillis)
                            .divide(BigDecimal.valueOf(3_600_000L), 4, RoundingMode.HALF_UP);
                    final BigDecimal timeEntryAccumulatedPay = timeEntryHourlyRate.multiply(timeEntryDurationHours);
                    totalAccumulatedPay = totalAccumulatedPay.add(timeEntryAccumulatedPay);
                    reportDayAccumulator.addAccumulatedPay(timeEntryAccumulatedPay);
                }
            } else {
                totalNonBillableTrackedTimeMillis += timeEntryDurationMillis;
                reportDayAccumulator.addNonBillableTrackedTimeMillis(timeEntryDurationMillis);
            }
        }
        return new Report<>(
                totalBillableTrackedTimeMillis,
                totalNonBillableTrackedTimeMillis,
                totalAccumulatedPay,
                reportDayAccumulators.entrySet().stream()
                        .map(entry -> new ReportDay<>(
                                entry.getKey(),
                                entry.getValue().billableTrackedTimeMillis(),
                                entry.getValue().nonBillableTrackedTimeMillis(),
                                entry.getValue().accumulatedPay(),
                                entry.getValue().trackedTimeMillisByKey().entrySet().stream()
                                        .map(reportEntryCreateFunction)
                                        .toList()))
                        .toList());
    }

}

class ReportDayAccumulator<U> {
    private final Map<U, Long> trackedTimeMillisByKey = new LinkedHashMap<>();
    private long billableTrackedTimeMillis;
    private long nonBillableTrackedTimeMillis;
    private BigDecimal accumulatedPay = BigDecimal.ZERO;

    public void addTrackedTimeMillis(U key, long trackedTimeMillis) {
        trackedTimeMillisByKey.merge(key, trackedTimeMillis, Long::sum);
    }

    public void addBillableTrackedTimeMillis(long trackedTimeMillis) {
        billableTrackedTimeMillis += trackedTimeMillis;
    }

    public void addNonBillableTrackedTimeMillis(long trackedTimeMillis) {
        nonBillableTrackedTimeMillis += trackedTimeMillis;
    }

    public void addAccumulatedPay(BigDecimal pay) {
        accumulatedPay = accumulatedPay.add(pay);
    }

    public Map<U, Long> trackedTimeMillisByKey() {
        return trackedTimeMillisByKey;
    }

    public long billableTrackedTimeMillis() {
        return billableTrackedTimeMillis;
    }

    public long nonBillableTrackedTimeMillis() {
        return nonBillableTrackedTimeMillis;
    }

    public BigDecimal accumulatedPay() {
        return accumulatedPay;
    }
}
