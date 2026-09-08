package me.eeshe.tempus.service.impl;

import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import me.eeshe.tempus.entity.Project;
import me.eeshe.tempus.entity.TimeEntry;
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
    public Report generateReport(ReportRequest reportRequest) {
        final List<TimeEntry> timeEntries = timeEntryRepository.findAll(TimeEntrySpecification.withFilters(
                reportRequest.startDate(),
                reportRequest.endDate(),
                reportRequest.projectIds(),
                reportRequest.taskIds(),
                reportRequest.descriptions(),
                reportRequest.isBillable()));

        final Map<Project, Long> projectTrackedTimeMillisMap = new HashMap<>();
        long totalTrackedTimeMillis = 0;
        for (TimeEntry timeEntry : timeEntries) {
            final long timeEntryDurationMillis = Duration.between(
                    timeEntry.getStartTime(),
                    timeEntry.getEndTime()).toMillis();
            projectTrackedTimeMillisMap.compute(timeEntry.getProject(), (_, projectTrackedTimeMillis) -> {
                if (projectTrackedTimeMillis == null) {
                    return timeEntryDurationMillis;
                }
                return projectTrackedTimeMillis + timeEntryDurationMillis;
            });
            totalTrackedTimeMillis += timeEntryDurationMillis;
        }
        return new Report(
                totalTrackedTimeMillis,
                projectTrackedTimeMillisMap.entrySet().stream().map(entry -> {
                    return new ProjectReportEntry(entry.getKey(), entry.getValue());
                }).toList());
    }

}
