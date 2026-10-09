package me.eeshe.tempus.integration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.assertj.MockMvcTester;
import org.springframework.test.web.servlet.assertj.MvcTestResult;

import jakarta.servlet.http.Cookie;
import me.eeshe.tempus.service.SyncService;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

@SpringBootTest
@AutoConfigureMockMvc
public class TempusApplicationIntegrationTest {
    private static final String REGISTER_URL = "/api/v1/auth/register";
    private static final String LOGIN_URL = "/api/v1/auth/login";
    private static final String LOGOUT_URL = "/api/v1/auth/logout";
    private static final String CURRENT_USER_URL = "/api/v1/auth/me";
    private static final String CLIENTS_URL = "/api/v1/clients";
    private static final String PROJECTS_URL = "/api/v1/projects";
    private static final String TASKS_URL = "/api/v1/tasks";
    private static final String TIME_ENTRIES_URL = "/api/v1/time-entries";
    private static final String PROJECT_REPORT_URL = "/api/v1/reports/projects";

    private static final String SESSION_COOKIE = "SESSION";
    private static final String PASSWORD = "MyPassword";

    private static final String CURRENT_USER_USERNAME = "CurrentUserIntegration";
    private static final String LOGOUT_USERNAME = "LogoutIntegration";
    private static final String DUPLICATE_USERNAME = "DuplicateIntegration";
    private static final String WORKFLOW_USERNAME = "WorkflowIntegration";

    @Autowired
    private MockMvcTester mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    /**
     * Neutralizes {@link me.eeshe.tempus.runner.AutoSyncRunner} and
     * {@link me.eeshe.tempus.scheduler.ExportScheduler}, so tests never touch the
     * real
     * data/sync snapshot files.
     */
    @MockitoBean
    private SyncService syncService;

    @Nested
    class Authentication {

        @Test
        void rejectsUnauthenticatedRequest() {
            assertThat(mockMvc.get().uri(CLIENTS_URL)).hasStatus(401);
        }

        @Test
        void registersLogsInAndChecksCurrentUser() throws Exception {
            final String credentialsJson = createCredentialsJson(CURRENT_USER_USERNAME);

            final MvcTestResult registerResult = mockMvc.post().uri(REGISTER_URL).with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(credentialsJson)
                    .exchange();

            assertThat(registerResult).hasStatus(201);
            assertThat(json(registerResult).get("name").asString()).isEqualTo(CURRENT_USER_USERNAME);

            final MvcTestResult loginResult = mockMvc.post().uri(LOGIN_URL).with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(credentialsJson)
                    .exchange();

            assertThat(loginResult).hasStatus(200);

            final MvcTestResult currentUserResult = mockMvc.get().uri(CURRENT_USER_URL)
                    .cookie(sessionCookie(loginResult))
                    .exchange();

            assertThat(currentUserResult).hasStatus(200);
            assertThat(json(currentUserResult).get("name").asString()).isEqualTo(CURRENT_USER_USERNAME);
        }

        @Test
        void logsOutUser() throws Exception {
            final Cookie sessionCookie = registerAndLogin(LOGOUT_USERNAME);

            assertThat(mockMvc.post().uri(LOGOUT_URL).with(csrf()).cookie(sessionCookie)).hasStatus(204);
            assertThat(mockMvc.get().uri(CURRENT_USER_URL).cookie(sessionCookie)).hasStatus(401);
        }

        @Test
        void rejectsDuplicateRegistration() throws Exception {
            final String credentialsJson = createCredentialsJson(DUPLICATE_USERNAME);

            assertThat(mockMvc.post().uri(REGISTER_URL).with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(credentialsJson)).hasStatus(201);

            final MvcTestResult registerResult = mockMvc.post().uri(REGISTER_URL).with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(credentialsJson)
                    .exchange();

            assertThat(registerResult).hasStatus(400);
            assertThat(json(registerResult).get("error").asString())
                    .isEqualTo("Username DuplicateIntegration is already used by another user");
        }
    }

    @Nested
    class TimeTracking {

        @Test
        void fullTimeTrackingWorkflow() throws Exception {
            final Cookie sessionCookie = registerAndLogin(WORKFLOW_USERNAME);

            final MvcTestResult clientResult = mockMvc.post().uri(CLIENTS_URL).with(csrf())
                    .cookie(sessionCookie)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("""
                            {"name": "MyClient"}""")
                    .exchange();

            assertThat(clientResult).hasStatus(201);
            final long clientId = json(clientResult).get("id").asLong();

            final String createProjectJson = """
                    {
                         "name": "MyProject",
                         "clientId": %s,
                         "hourlyRate": 50.00
                     }""".formatted(clientId);
            final MvcTestResult projectResult = mockMvc.post().uri(PROJECTS_URL).with(csrf())
                    .cookie(sessionCookie)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(createProjectJson)
                    .exchange();

            assertThat(projectResult).hasStatus(201);
            final long projectId = json(projectResult).get("id").asLong();

            final String createTaskJson = """
                    {
                        "name": "MyTask",
                        "projectId": %s
                    }""".formatted(projectId);
            final MvcTestResult taskResult = mockMvc.post().uri(TASKS_URL).with(csrf())
                    .cookie(sessionCookie)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(createTaskJson)
                    .exchange();

            assertThat(taskResult).hasStatus(201);
            final long taskId = json(taskResult).get("id").asLong();

            final String createTimeEntryJson = """
                    {
                        "projectId": %s,
                        "taskId": %s,
                        "description": "MyDescription",
                        "isBillable": true,
                        "startTime": "2026-01-01T09:00:00Z",
                        "endTime": "2026-01-01T11:00:00Z"
                    }""".formatted(projectId, taskId);
            final MvcTestResult timeEntryResult = mockMvc.post().uri(TIME_ENTRIES_URL).with(csrf())
                    .cookie(sessionCookie)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(createTimeEntryJson)
                    .exchange();

            assertThat(timeEntryResult).hasStatus(201);

            final MvcTestResult listResult = mockMvc.get().uri(TIME_ENTRIES_URL)
                    .cookie(sessionCookie)
                    .exchange();

            assertThat(listResult).hasStatus(200);
            assertThat(json(listResult).get("totalElements").asLong()).isPositive();

            final String reportJson = """
                    {
                        "startDate": "2026-01-01T00:00:00Z",
                        "endDate": "2026-01-02T00:00:00Z",
                        "projectIds": [],
                        "taskIds": [],
                        "clientIds": [],
                        "descriptions": [],
                        "isBillable": true
                    }""";
            final MvcTestResult reportResult = mockMvc.post().uri(PROJECT_REPORT_URL).with(csrf())
                    .cookie(sessionCookie)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(reportJson)
                    .exchange();

            assertThat(reportResult).hasStatus(200);

            final JsonNode report = json(reportResult);
            assertThat(report.get("totalTrackedTimeMillis").asLong()).isEqualTo(7_200_000L);
            assertThat(report.get("totalBillableTrackedTimeMillis").asLong()).isEqualTo(7_200_000L);
            assertThat(report.get("totalNonBillableTrackedTimeMillis").asLong()).isZero();
            assertThat(report.get("totalAccumulatedPay").decimalValue()).isEqualByComparingTo("100.00");
        }
    }

    private Cookie registerAndLogin(String username) throws Exception {
        final String credentialsJson = createCredentialsJson(username);

        assertThat(mockMvc.post().uri(REGISTER_URL).with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(credentialsJson)).hasStatus(201);

        final MvcTestResult loginResult = mockMvc.post().uri(LOGIN_URL).with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(credentialsJson)
                .exchange();

        assertThat(loginResult).hasStatus(200);

        return sessionCookie(loginResult);
    }

    private static Cookie sessionCookie(MvcTestResult loginResult) {
        return loginResult.getResponse().getCookie(SESSION_COOKIE);
    }

    private static String createCredentialsJson(String username) {
        return """
                {
                    "username": "%s",
                    "password": "%s"
                }""".formatted(username, PASSWORD);
    }

    private JsonNode json(MvcTestResult result) throws Exception {
        return objectMapper.readTree(result.getResponse().getContentAsString());
    }
}
