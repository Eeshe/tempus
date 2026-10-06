package me.eeshe.tempus.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.jpa.domain.PredicateSpecification;
import org.springframework.test.util.ReflectionTestUtils;

import me.eeshe.tempus.entity.Client;
import me.eeshe.tempus.entity.Project;
import me.eeshe.tempus.entity.TimeEntry;
import me.eeshe.tempus.model.ClientReportEntry;
import me.eeshe.tempus.model.ProjectReportEntry;
import me.eeshe.tempus.model.Report;
import me.eeshe.tempus.repository.TimeEntryRepository;
import me.eeshe.tempus.request.ReportRequest;
import me.eeshe.tempus.support.EntityTestBase;

@ExtendWith(MockitoExtension.class)
public class ReportServiceImplTest extends EntityTestBase {
    private static final long ONE_HOUR_MILLIS = 3_600_000L;
    private static final long TWO_HOURS_MILLIS = 7_200_000L;
    private static final long THREE_HOURS_MILLIS = 10_800_000L;

    private static final BigDecimal HOURLY_RATE = new BigDecimal("50.00");
    private static final BigDecimal OTHER_HOURLY_RATE = new BigDecimal("75.00");

    private static final Instant START_TIME = Instant.parse("2026-01-01T08:00:00Z");
    private static final Instant REPORT_START = Instant.parse("2026-01-01T00:00:00Z");
    private static final Instant REPORT_END = Instant.parse("2026-01-02T00:00:00Z");

    private static final ReportRequest REPORT_REQUEST = new ReportRequest(
            USER_ID,
            REPORT_START,
            REPORT_END,
            List.of(),
            List.of(),
            List.of(),
            List.of(),
            null);

    @Mock
    private TimeEntryRepository timeEntryRepository;

    @InjectMocks
    private ReportServiceImpl reportService;

    @Nested
    class GenerateProjectReport {

        @Test
        void aggregatesTrackedTimeByProject() {
            final Client client = createClient();
            final Project project = createProject(PROJECT_ID, HOURLY_RATE, client);
            final TimeEntry firstEntry = createTimeEntry(project, true, START_TIME, START_TIME.plusSeconds(3600));
            final TimeEntry secondEntry = createTimeEntry(
                    project, true, START_TIME.plusSeconds(3600), START_TIME.plusSeconds(10800));

            when(timeEntryRepository.findAll(any(PredicateSpecification.class)))
                    .thenReturn(List.of(firstEntry, secondEntry));

            final Report<ProjectReportEntry> report = reportService.generateProjectReport(REPORT_REQUEST);

            assertThat(report.totalTrackedTimeMillis()).isEqualTo(THREE_HOURS_MILLIS);
            assertThat(report.totalBillableTrackedTimeMillis()).isEqualTo(THREE_HOURS_MILLIS);
            assertThat(report.totalNonBillableTrackedTimeMillis()).isZero();
            assertThat(report.reportEntries()).containsExactly(new ProjectReportEntry(project, THREE_HOURS_MILLIS));

            verify(timeEntryRepository).findAll(any(PredicateSpecification.class));
        }

        @Test
        void createsSeparateEntryPerProject() {
            final Client client = createClient();

            final Project project = createProject(PROJECT_ID, HOURLY_RATE, client);
            final Project secondProject = createProject(SECOND_PROJECT_ID, HOURLY_RATE, client);

            final TimeEntry firstEntry = createTimeEntry(project, true, START_TIME, START_TIME.plusSeconds(3600));
            final TimeEntry secondEntry = createTimeEntry(secondProject, true, START_TIME,
                    START_TIME.plusSeconds(3600));

            when(timeEntryRepository.findAll(any(PredicateSpecification.class)))
                    .thenReturn(List.of(firstEntry, secondEntry));

            final Report<ProjectReportEntry> report = reportService.generateProjectReport(REPORT_REQUEST);

            assertThat(report.reportEntries()).containsExactlyInAnyOrder(
                    new ProjectReportEntry(project, ONE_HOUR_MILLIS),
                    new ProjectReportEntry(secondProject, ONE_HOUR_MILLIS));
        }

        @Test
        void separatesBillableAndNonBillableTotals() {
            final Client client = createClient();
            final Project project = createProject(PROJECT_ID, HOURLY_RATE, client);

            final TimeEntry billableEntry = createTimeEntry(project, true, START_TIME, START_TIME.plusSeconds(3600));
            final TimeEntry nonBillableEntry = createTimeEntry(
                    project, false, START_TIME.plusSeconds(3600), START_TIME.plusSeconds(10800));

            when(timeEntryRepository.findAll(any(PredicateSpecification.class)))
                    .thenReturn(List.of(billableEntry, nonBillableEntry));

            final Report<ProjectReportEntry> report = reportService.generateProjectReport(REPORT_REQUEST);

            assertThat(report.totalTrackedTimeMillis()).isEqualTo(THREE_HOURS_MILLIS);
            assertThat(report.totalBillableTrackedTimeMillis()).isEqualTo(ONE_HOUR_MILLIS);
            assertThat(report.totalNonBillableTrackedTimeMillis()).isEqualTo(TWO_HOURS_MILLIS);
        }

        @Test
        void accumulatesPayOnlyForBillableEntriesWithHourlyRate() {
            final Client client = createClient();

            final Project project = createProject(PROJECT_ID, HOURLY_RATE, client);
            final Project nullRateProject = createProject(SECOND_PROJECT_ID, null, client);
            final Project nonBillableProject = createProject(22L, OTHER_HOURLY_RATE, client);

            final TimeEntry billableEntry = createTimeEntry(project, true, START_TIME, START_TIME.plusSeconds(3600));
            final TimeEntry nullRateEntry = createTimeEntry(
                    nullRateProject, true, START_TIME, START_TIME.plusSeconds(3600));
            final TimeEntry nonBillableEntry = createTimeEntry(
                    nonBillableProject, false, START_TIME, START_TIME.plusSeconds(3600));

            when(timeEntryRepository.findAll(any(PredicateSpecification.class)))
                    .thenReturn(List.of(billableEntry, nullRateEntry, nonBillableEntry));

            final Report<ProjectReportEntry> report = reportService.generateProjectReport(REPORT_REQUEST);

            assertThat(report.totalAccumulatedPay()).isEqualByComparingTo(HOURLY_RATE);
        }

        @Test
        void accumulatesPayAcrossMultipleEntries() {
            final Client client = createClient();
            final Project project = createProject(PROJECT_ID, HOURLY_RATE, client);

            final TimeEntry firstEntry = createTimeEntry(project, true, START_TIME, START_TIME.plusSeconds(3600));
            final TimeEntry secondEntry = createTimeEntry(
                    project, true, START_TIME.plusSeconds(3600), START_TIME.plusSeconds(10800));

            when(timeEntryRepository.findAll(any(PredicateSpecification.class)))
                    .thenReturn(List.of(firstEntry, secondEntry));

            final Report<ProjectReportEntry> report = reportService.generateProjectReport(REPORT_REQUEST);

            assertThat(report.totalAccumulatedPay()).isEqualByComparingTo(new BigDecimal("150.00"));
        }

        @Test
        void accumulatesPayProportionallyToDuration() {
            final Client client = createClient();
            final Project project = createProject(PROJECT_ID, HOURLY_RATE, client);
            final TimeEntry timeEntry = createTimeEntry(project, true, START_TIME, START_TIME.plusSeconds(5400));

            when(timeEntryRepository.findAll(any(PredicateSpecification.class))).thenReturn(List.of(timeEntry));

            final Report<ProjectReportEntry> report = reportService.generateProjectReport(REPORT_REQUEST);

            assertThat(report.totalAccumulatedPay()).isEqualByComparingTo(new BigDecimal("75.00"));
        }

        @Test
        void returnsEmptyReportWhenNoEntries() {
            when(timeEntryRepository.findAll(any(PredicateSpecification.class))).thenReturn(List.of());

            final Report<ProjectReportEntry> report = reportService.generateProjectReport(REPORT_REQUEST);

            assertThat(report.totalTrackedTimeMillis()).isZero();
            assertThat(report.totalBillableTrackedTimeMillis()).isZero();
            assertThat(report.totalNonBillableTrackedTimeMillis()).isZero();
            assertThat(report.totalAccumulatedPay()).isEqualByComparingTo(BigDecimal.ZERO);
            assertThat(report.reportEntries()).isEmpty();
        }
    }

    @Nested
    class GenerateClientReport {

        @Test
        void aggregatesTrackedTimeByClient() {
            final Client client = createClient();

            final Project project = createProject(PROJECT_ID, HOURLY_RATE, client);
            final Project secondProject = createProject(SECOND_PROJECT_ID, HOURLY_RATE, client);

            final TimeEntry firstEntry = createTimeEntry(project, true, START_TIME, START_TIME.plusSeconds(3600));
            final TimeEntry secondEntry = createTimeEntry(
                    secondProject, true, START_TIME.plusSeconds(3600), START_TIME.plusSeconds(10800));

            when(timeEntryRepository.findAll(any(PredicateSpecification.class)))
                    .thenReturn(List.of(firstEntry, secondEntry));

            final Report<ClientReportEntry> report = reportService.generateClientReport(REPORT_REQUEST);

            assertThat(report.totalTrackedTimeMillis()).isEqualTo(THREE_HOURS_MILLIS);
            assertThat(report.totalBillableTrackedTimeMillis()).isEqualTo(THREE_HOURS_MILLIS);
            assertThat(report.totalNonBillableTrackedTimeMillis()).isZero();
            assertThat(report.reportEntries()).containsExactly(new ClientReportEntry(client, THREE_HOURS_MILLIS));
        }

        @Test
        void skipsEntriesWithoutClient() {
            final Project project = createProject(PROJECT_ID, HOURLY_RATE, null);
            final TimeEntry timeEntry = createTimeEntry(project, true, START_TIME, START_TIME.plusSeconds(3600));

            when(timeEntryRepository.findAll(any(PredicateSpecification.class))).thenReturn(List.of(timeEntry));

            final Report<ClientReportEntry> report = reportService.generateClientReport(REPORT_REQUEST);

            assertThat(report.totalTrackedTimeMillis()).isZero();
            assertThat(report.totalBillableTrackedTimeMillis()).isZero();
            assertThat(report.totalAccumulatedPay()).isEqualByComparingTo(BigDecimal.ZERO);
            assertThat(report.reportEntries()).isEmpty();
        }
    }

    private static Project createProject(long id, BigDecimal hourlyRate, Client client) {
        final Project project = new Project("MyProject", createUser(USER_ID), hourlyRate, client);
        ReflectionTestUtils.setField(project, "id", id);

        return project;
    }

    private static TimeEntry createTimeEntry(Project project, boolean isBillable, Instant startTime, Instant endTime) {
        return new TimeEntry(createUser(USER_ID), project, null, "MyDescription", isBillable, startTime, endTime);
    }
}
