package me.eeshe.tempus.service.impl;

import java.time.Duration;
import java.util.List;

import org.springframework.stereotype.Service;

import me.eeshe.tempus.entity.TimeEntry;
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
    public Report generateReport(ReportRequest reportRequest) {
        final List<TimeEntry> timeEntries = timeEntryRepository.findAll(TimeEntrySpecification.withFilters(
                reportRequest.startDate(),
                reportRequest.endDate(),
                reportRequest.projectIds(),
                reportRequest.descriptions(),
                reportRequest.isBillable()));

        final long totalTrackedTimeMillis = timeEntries.stream()
                .mapToLong(timeEntry -> Duration.between(timeEntry.getStartTime(), timeEntry.getEndTime()).toMillis())
                .sum();
        return new Report(
                timeEntries,
                totalTrackedTimeMillis);
    }

}
