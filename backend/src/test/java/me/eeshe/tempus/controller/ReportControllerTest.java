package me.eeshe.tempus.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.List;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import me.eeshe.tempus.dto.ClientReportEntryDTO;
import me.eeshe.tempus.dto.ProjectReportEntryDTO;
import me.eeshe.tempus.dto.ReportDTO;
import me.eeshe.tempus.dto.ReportDayDTO;
import me.eeshe.tempus.dto.ReportRequestDTO;
import me.eeshe.tempus.exception.InvalidZoneIdException;
import me.eeshe.tempus.mapper.ReportMapper;
import me.eeshe.tempus.model.ClientReportEntry;
import me.eeshe.tempus.model.ProjectReportEntry;
import me.eeshe.tempus.model.Report;
import me.eeshe.tempus.model.ReportDay;
import me.eeshe.tempus.request.ReportRequest;
import me.eeshe.tempus.service.ReportService;
import me.eeshe.tempus.support.ControllerTestBase;

@WebMvcTest(ReportController.class)
public class ReportControllerTest extends ControllerTestBase {
    private static final long TRACKED_TIME_MILLIS = 3_600_000L;

    private static final Instant START_DATE = Instant.parse("2026-01-02T00:00:00Z");
    private static final Instant END_DATE = Instant.parse("2026-01-02T10:00:00Z");
    private static final LocalDate REPORT_DATE = LocalDate.parse("2026-01-02");
    private static final String ZONE_ID = "UTC";

    private static final BigDecimal TOTAL_ACCUMULATED_PAY = new BigDecimal("100");

    private static final String REPORT_REQUEST_JSON_BODY = """
            {
                "startDate": "%s",
                "endDate": "%s",
                "projectIds": [%s],
                "taskIds": [%s],
                "clientIds": [%s],
                "descriptions": ["MyDescription"],
                "isBillable": true,
                "zoneId": "%s"
            }""".formatted(START_DATE, END_DATE, PROJECT_ID, TASK_ID, CLIENT_ID, ZONE_ID);

    @MockitoBean
    private ReportService reportService;

    @MockitoBean
    private ReportMapper reportMapper;

    @Nested
    class GetProjectReport {
        private static final String URL = "/api/v1/reports/projects";

        @Test
        void returnsProjectReport() {
            final ReportRequest reportRequest = createReportRequest();
            final ReportRequestDTO reportRequestDTO = createReportRequestDTO();
            final Report<ProjectReportEntry> report = createProjectReport();
            final ReportDTO<ProjectReportEntryDTO> reportDTO = createProjectReportDTO();

            when(reportMapper.fromDTO(
                    eq(reportRequestDTO),
                    eq(USER_ID)))
                    .thenReturn(reportRequest);
            when(reportService.generateProjectReport(reportRequest)).thenReturn(report);
            when(reportMapper.toProjectDTO(report)).thenReturn(reportDTO);

            assertThat(mockMvc.post().uri(URL).with(createPrincipalWithCsrf(USER_ID))
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(REPORT_REQUEST_JSON_BODY))
                    .hasStatus(200)
                    .bodyJson()
                    .isEqualTo(createProjectReportDTOJson());

            verify(reportMapper).fromDTO(
                    eq(reportRequestDTO),
                    eq(USER_ID));
            verify(reportService).generateProjectReport(reportRequest);
            verify(reportMapper).toProjectDTO(report);
        }

        @Test
        void returnsEmptyProjectReport() {
            final ReportRequest reportRequest = createReportRequest();
            final ReportRequestDTO reportRequestDTO = createReportRequestDTO();
            final Report<ProjectReportEntry> report = createEmptyProjectReport();
            final ReportDTO<ProjectReportEntryDTO> reportDTO = createEmptyProjectReportDTO();

            when(reportMapper.fromDTO(
                    eq(reportRequestDTO),
                    eq(USER_ID)))
                    .thenReturn(reportRequest);
            when(reportService.generateProjectReport(reportRequest)).thenReturn(report);
            when(reportMapper.toProjectDTO(report)).thenReturn(reportDTO);

            assertThat(mockMvc.post().uri(URL).with(createPrincipalWithCsrf(USER_ID))
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(REPORT_REQUEST_JSON_BODY))
                    .hasStatus(200)
                    .bodyJson()
                    .isEqualTo(createEmptyProjectReportDTOJson());

            verify(reportService).generateProjectReport(reportRequest);
        }

        @Test
        void rejectsInvalidZoneId() {
            final ReportRequestDTO reportRequestDTO = createReportRequestDTO();
            final InvalidZoneIdException exception = new InvalidZoneIdException("Invalid/ZoneId");

            when(reportMapper.fromDTO(
                    eq(reportRequestDTO),
                    eq(USER_ID)))
                    .thenThrow(exception);

            assertThat(mockMvc.post().uri(URL).with(createPrincipalWithCsrf(USER_ID))
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(REPORT_REQUEST_JSON_BODY))
                    .hasStatus(400)
                    .bodyJson()
                    .extractingPath("$.error").asString()
                    .isEqualTo(exception.getMessage());

            verify(reportMapper).fromDTO(
                    eq(reportRequestDTO),
                    eq(USER_ID));
            verifyNoInteractions(reportService);
        }

        @Test
        void rejectsUnauthenticatedRequest() {
            assertThat(mockMvc.post().uri(URL).with(createCsrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(REPORT_REQUEST_JSON_BODY))
                    .hasStatus(401);

            verifyNoInteractions(reportMapper, reportService);
        }
    }

    @Nested
    class GetClientReport {
        private static final String URL = "/api/v1/reports/clients";

        @Test
        void returnsClientReport() {
            final ReportRequest reportRequest = createReportRequest();
            final ReportRequestDTO reportRequestDTO = createReportRequestDTO();
            final Report<ClientReportEntry> report = createClientReport();
            final ReportDTO<ClientReportEntryDTO> reportDTO = createClientReportDTO();

            when(reportMapper.fromDTO(
                    eq(reportRequestDTO),
                    eq(USER_ID)))
                    .thenReturn(reportRequest);
            when(reportService.generateClientReport(reportRequest)).thenReturn(report);
            when(reportMapper.toClientDTO(report)).thenReturn(reportDTO);

            assertThat(mockMvc.post().uri(URL).with(createPrincipalWithCsrf(USER_ID))
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(REPORT_REQUEST_JSON_BODY))
                    .hasStatus(200)
                    .bodyJson()
                    .isEqualTo(createClientReportDTOJson());

            verify(reportMapper).fromDTO(
                    eq(reportRequestDTO),
                    eq(USER_ID));
            verify(reportService).generateClientReport(reportRequest);
            verify(reportMapper).toClientDTO(report);
        }

        @Test
        void returnsEmptyClientReport() {
            final ReportRequest reportRequest = createReportRequest();
            final ReportRequestDTO reportRequestDTO = createReportRequestDTO();
            final Report<ClientReportEntry> report = createEmptyClientReport();
            final ReportDTO<ClientReportEntryDTO> reportDTO = createEmptyClientReportDTO();

            when(reportMapper.fromDTO(
                    eq(reportRequestDTO),
                    eq(USER_ID)))
                    .thenReturn(reportRequest);
            when(reportService.generateClientReport(reportRequest)).thenReturn(report);
            when(reportMapper.toClientDTO(report)).thenReturn(reportDTO);

            assertThat(mockMvc.post().uri(URL).with(createPrincipalWithCsrf(USER_ID))
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(REPORT_REQUEST_JSON_BODY))
                    .hasStatus(200)
                    .bodyJson()
                    .isEqualTo(createEmptyClientReportDTOJson());

            verify(reportService).generateClientReport(reportRequest);
        }

        @Test
        void rejectsInvalidZoneId() {
            final ReportRequestDTO reportRequestDTO = createReportRequestDTO();
            final InvalidZoneIdException exception = new InvalidZoneIdException("Invalid/ZoneId");

            when(reportMapper.fromDTO(
                    eq(reportRequestDTO),
                    eq(USER_ID)))
                    .thenThrow(exception);

            assertThat(mockMvc.post().uri(URL).with(createPrincipalWithCsrf(USER_ID))
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(REPORT_REQUEST_JSON_BODY))
                    .hasStatus(400)
                    .bodyJson()
                    .extractingPath("$.error").asString()
                    .isEqualTo(exception.getMessage());

            verify(reportMapper).fromDTO(
                    eq(reportRequestDTO),
                    eq(USER_ID));
            verifyNoInteractions(reportService);
        }

        @Test
        void rejectsUnauthenticatedRequest() {
            assertThat(mockMvc.post().uri(URL).with(createCsrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(REPORT_REQUEST_JSON_BODY))
                    .hasStatus(401);

            verifyNoInteractions(reportMapper, reportService);
        }
    }

    private static ReportRequestDTO createReportRequestDTO() {
        return new ReportRequestDTO(
                START_DATE,
                END_DATE,
                List.of(PROJECT_ID),
                List.of(TASK_ID),
                List.of(CLIENT_ID),
                List.of("MyDescription"),
                true,
                ZONE_ID);
    }

    private static ReportRequest createReportRequest() {
        return new ReportRequest(
                USER_ID,
                START_DATE,
                END_DATE,
                List.of(PROJECT_ID),
                List.of(TASK_ID),
                List.of(CLIENT_ID),
                List.of("MyDescription"),
                true,
                ZoneOffset.UTC);
    }

    private static Report<ProjectReportEntry> createProjectReport() {
        return new Report<>(
                TRACKED_TIME_MILLIS,
                0L,
                TOTAL_ACCUMULATED_PAY,
                List.of(new ReportDay<>(
                        REPORT_DATE,
                        TRACKED_TIME_MILLIS,
                        0L,
                        TOTAL_ACCUMULATED_PAY,
                        List.of(new ProjectReportEntry(createProject(), TRACKED_TIME_MILLIS)))));
    }

    private static Report<ProjectReportEntry> createEmptyProjectReport() {
        return new Report<>(
                0L,
                0L,
                BigDecimal.ZERO,
                List.of());
    }

    private static ReportDTO<ProjectReportEntryDTO> createProjectReportDTO() {
        return new ReportDTO<>(
                TRACKED_TIME_MILLIS,
                TRACKED_TIME_MILLIS,
                0L,
                TOTAL_ACCUMULATED_PAY,
                List.of(new ReportDayDTO<>(
                        REPORT_DATE,
                        TRACKED_TIME_MILLIS,
                        TRACKED_TIME_MILLIS,
                        0L,
                        TOTAL_ACCUMULATED_PAY,
                        List.of(new ProjectReportEntryDTO(createProjectDTO(), TRACKED_TIME_MILLIS)))));
    }

    private static ReportDTO<ProjectReportEntryDTO> createEmptyProjectReportDTO() {
        return new ReportDTO<>(
                0L,
                0L,
                0L,
                BigDecimal.ZERO,
                List.of());
    }

    private static Report<ClientReportEntry> createClientReport() {
        return new Report<>(
                TRACKED_TIME_MILLIS,
                0L,
                TOTAL_ACCUMULATED_PAY,
                List.of(new ReportDay<>(
                        REPORT_DATE,
                        TRACKED_TIME_MILLIS,
                        0L,
                        TOTAL_ACCUMULATED_PAY,
                        List.of(new ClientReportEntry(createClient(), TRACKED_TIME_MILLIS)))));
    }

    private static Report<ClientReportEntry> createEmptyClientReport() {
        return new Report<>(
                0L,
                0L,
                BigDecimal.ZERO,
                List.of());
    }

    private static ReportDTO<ClientReportEntryDTO> createClientReportDTO() {
        return new ReportDTO<>(
                TRACKED_TIME_MILLIS,
                TRACKED_TIME_MILLIS,
                0L,
                TOTAL_ACCUMULATED_PAY,
                List.of(new ReportDayDTO<>(
                        REPORT_DATE,
                        TRACKED_TIME_MILLIS,
                        TRACKED_TIME_MILLIS,
                        0L,
                        TOTAL_ACCUMULATED_PAY,
                        List.of(new ClientReportEntryDTO(createClientDTO(), TRACKED_TIME_MILLIS)))));
    }

    private static ReportDTO<ClientReportEntryDTO> createEmptyClientReportDTO() {
        return new ReportDTO<>(
                0L,
                0L,
                0L,
                BigDecimal.ZERO,
                List.of());
    }

    private static String createProjectReportDTOJson() {
        return createReportDTOJson(
                "[%s]".formatted(createProjectReportDayDTOJson()),
                TRACKED_TIME_MILLIS,
                TRACKED_TIME_MILLIS,
                0L,
                TOTAL_ACCUMULATED_PAY);
    }

    private static String createEmptyProjectReportDTOJson() {
        return createReportDTOJson("[]", 0L, 0L, 0L, BigDecimal.ZERO);
    }

    private static String createReportDTOJson(
            String reportDaysJson,
            long totalTrackedTimeMillis,
            long totalBillableTrackedTimeMillis,
            long totalNonBillableTrackedTimeMillis,
            BigDecimal totalAccumulatedPay) {
        return """
                {
                    "totalTrackedTimeMillis": %s,
                    "totalBillableTrackedTimeMillis": %s,
                    "totalNonBillableTrackedTimeMillis": %s,
                    "totalAccumulatedPay": %s,
                    "reportDays": %s
                }""".formatted(
                totalTrackedTimeMillis,
                totalBillableTrackedTimeMillis,
                totalNonBillableTrackedTimeMillis,
                totalAccumulatedPay,
                reportDaysJson);
    }

    private static String createProjectReportDayDTOJson() {
        return """
                {
                    "date": "%s",
                    "totalTrackedTimeMillis": %s,
                    "totalBillableTrackedTimeMillis": %s,
                    "totalNonBillableTrackedTimeMillis": %s,
                    "totalAccumulatedPay": %s,
                    "reportEntries": [%s]
                }""".formatted(
                REPORT_DATE,
                TRACKED_TIME_MILLIS,
                TRACKED_TIME_MILLIS,
                0L,
                TOTAL_ACCUMULATED_PAY,
                createProjectReportEntryDTOJson());
    }

    private static String createProjectReportEntryDTOJson() {
        return """
                {
                    "project": %s,
                    "trackedTimeMillis": %s
                }""".formatted(createProjectDTOJson(), TRACKED_TIME_MILLIS);
    }

    private static String createClientReportDTOJson() {
        return createReportDTOJson(
                "[%s]".formatted(createClientReportDayDTOJson()),
                TRACKED_TIME_MILLIS,
                TRACKED_TIME_MILLIS,
                0L,
                TOTAL_ACCUMULATED_PAY);
    }

    private static String createEmptyClientReportDTOJson() {
        return createReportDTOJson("[]", 0L, 0L, 0L, BigDecimal.ZERO);
    }

    private static String createClientReportDayDTOJson() {
        return """
                {
                    "date": "%s",
                    "totalTrackedTimeMillis": %s,
                    "totalBillableTrackedTimeMillis": %s,
                    "totalNonBillableTrackedTimeMillis": %s,
                    "totalAccumulatedPay": %s,
                    "reportEntries": [%s]
                }""".formatted(
                REPORT_DATE,
                TRACKED_TIME_MILLIS,
                TRACKED_TIME_MILLIS,
                0L,
                TOTAL_ACCUMULATED_PAY,
                createClientReportEntryDTOJson());
    }

    private static String createClientReportEntryDTOJson() {
        return """
                {
                    "client": %s,
                    "trackedTimeMillis": %s
                }""".formatted(createClientDTOJson(), TRACKED_TIME_MILLIS);
    }

}
