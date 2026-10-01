package me.eeshe.tempus;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.List;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.openapitools.jackson.nullable.JsonNullable;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.util.ReflectionTestUtils;

import me.eeshe.tempus.controller.TimeEntryController;
import me.eeshe.tempus.dto.CreateTimeEntryRequestDTO;
import me.eeshe.tempus.dto.PatchTimeEntryRequestDTO;
import me.eeshe.tempus.dto.ProjectDTO;
import me.eeshe.tempus.dto.TaskDTO;
import me.eeshe.tempus.dto.TimeEntryDTO;
import me.eeshe.tempus.dto.TimeEntryPageDTO;
import me.eeshe.tempus.entity.Project;
import me.eeshe.tempus.entity.Task;
import me.eeshe.tempus.entity.TimeEntry;
import me.eeshe.tempus.exception.TimeEntryNotFoundException;
import me.eeshe.tempus.mapper.TimeEntryMapper;
import me.eeshe.tempus.mapper.TimeEntryPageMapper;
import me.eeshe.tempus.model.TimeEntryPage;
import me.eeshe.tempus.request.CreateTimeEntryRequest;
import me.eeshe.tempus.request.PatchTimeEntryRequest;
import me.eeshe.tempus.service.TimeEntryService;
import me.eeshe.tempus.support.ControllerTestBase;

@WebMvcTest(TimeEntryController.class)
public class TimeEntryControllerTest extends ControllerTestBase {
    private static final long USER_ID = 1L;
    private static final long PROJECT_ID = 20L;
    private static final long TASK_ID = 300L;
    private static final long TIME_ENTRY_ID = 400L;

    private static final Instant CREATED_AT = Instant.parse("2026-01-01T10:00:00Z");
    private static final Instant START_TIME = Instant.parse("2026-01-02T09:00:00Z");
    private static final Instant END_TIME = Instant.parse("2026-01-02T10:00:00Z");
    private static final Instant CURSOR = Instant.parse("2026-01-01T00:00:00Z");

    private static final String TIME_ENTRY_PROJECT_NULL_ERROR_MESSAGE = "Time entry project can't be null";
    private static final String TIME_ENTRY_BILLABLE_STATUS_NULL_ERROR_MESSAGE = "Time entry billable status can't be null";
    private static final String TIME_ENTRY_START_TIME_NULL_ERROR_MESSAGE = "Time entry start time can't be null";
    private static final String TIME_ENTRY_PROJECT_NULL_IF_PROVIDED_ERROR_MESSAGE = "Time entry project can't be null if provided";
    private static final String TIME_ENTRY_BILLABLE_STATUS_NULL_IF_PROVIDED_ERROR_MESSAGE = "Time entry billable status can't be null if provided";
    private static final String TIME_ENTRY_START_TIME_NULL_IF_PROVIDED_ERROR_MESSAGE = "Time entry start time can't be null if provided";

    private static final String CREATE_TIME_ENTRY_JSON_BODY = """
            {
                "projectId": %s,
                "taskId": %s,
                "description": "MyDescription",
                "isBillable": true,
                "startTime": "%s",
                "endTime": "%s"
            }""".formatted(PROJECT_ID, TASK_ID, START_TIME, END_TIME);

    @MockitoBean
    private TimeEntryService timeEntryService;

    @MockitoBean
    private TimeEntryMapper timeEntryMapper;

    @MockitoBean
    private TimeEntryPageMapper timeEntryPageMapper;

    @Nested
    class ListTimeEntries {
        private static final String URL = "/api/v1/time-entries";

        @Test
        void returnsAuthenticatedUserTimeEntries() {
            final TimeEntryPage timeEntryPage = createTimeEntryPage();

            when(timeEntryService.listTimeEntries(USER_ID, null, 50)).thenReturn(timeEntryPage);
            when(timeEntryPageMapper.toDTO(timeEntryPage)).thenReturn(createTimeEntryPageDTO());

            assertThat(mockMvc.get().uri(URL).with(createPrincipal(USER_ID)))
                    .hasStatus(200)
                    .bodyJson()
                    .isEqualTo(createTimeEntryPageDTOJson());

            verify(timeEntryService).listTimeEntries(USER_ID, null, 50);
        }

        @Test
        void returnsAuthenticatedUserEmptyTimeEntries() {
            final TimeEntryPage timeEntryPage = createEmptyTimeEntryPage();

            when(timeEntryService.listTimeEntries(USER_ID, null, 50)).thenReturn(timeEntryPage);
            when(timeEntryPageMapper.toDTO(timeEntryPage)).thenReturn(createEmptyTimeEntryPageDTO());

            assertThat(mockMvc.get().uri(URL).with(createPrincipal(USER_ID)))
                    .hasStatus(200)
                    .bodyJson()
                    .isEqualTo(createEmptyTimeEntryPageDTOJson());

            verify(timeEntryService).listTimeEntries(USER_ID, null, 50);
        }

        @Test
        void returnsAuthenticatedUserTimeEntriesWithCursor() {
            final TimeEntryPage timeEntryPage = createTimeEntryPage();

            when(timeEntryService.listTimeEntries(USER_ID, CURSOR, 2)).thenReturn(timeEntryPage);
            when(timeEntryPageMapper.toDTO(timeEntryPage)).thenReturn(createTimeEntryPageDTO());

            assertThat(mockMvc.get().uri(URL)
                    .param("cursor", CURSOR.toString())
                    .param("size", "2")
                    .with(createPrincipal(USER_ID)))
                    .hasStatus(200)
                    .bodyJson()
                    .isEqualTo(createTimeEntryPageDTOJson());

            verify(timeEntryService).listTimeEntries(USER_ID, CURSOR, 2);
        }

        @Test
        void rejectsUnauthenticatedRequest() {
            assertThat(mockMvc.get().uri(URL)).hasStatus(401);

            verifyNoInteractions(timeEntryService);
        }
    }

    @Nested
    class GetTimeEntry {
        private static final String URL = "/api/v1/time-entries/{timeEntryId}";

        @Test
        void returnsAuthenticatedUserTimeEntry() {
            final TimeEntry timeEntry = createTimeEntry();

            when(timeEntryService.getTimeEntry(USER_ID, TIME_ENTRY_ID)).thenReturn(timeEntry);
            when(timeEntryMapper.toDTO(timeEntry)).thenReturn(createTimeEntryDTO());

            assertThat(mockMvc.get().uri(URL, TIME_ENTRY_ID).with(createPrincipal(USER_ID)))
                    .hasStatus(200)
                    .bodyJson()
                    .isEqualTo(createTimeEntryDTOJson());

            verify(timeEntryService).getTimeEntry(USER_ID, TIME_ENTRY_ID);
        }

        @Test
        void rejectsUnauthenticatedRequest() {
            assertThat(mockMvc.get().uri(URL, TIME_ENTRY_ID)).hasStatus(401);

            verifyNoInteractions(timeEntryService);
        }

        @Test
        void rejectsNonExistentTimeEntry() {
            when(timeEntryService.getTimeEntry(USER_ID, TIME_ENTRY_ID))
                    .thenThrow(new TimeEntryNotFoundException(TIME_ENTRY_ID));

            assertThat(mockMvc.get().uri(URL, TIME_ENTRY_ID).with(createPrincipal(USER_ID)))
                    .hasStatus(404)
                    .bodyJson()
                    .extractingPath("$.error").asString()
                    .isEqualTo(new TimeEntryNotFoundException(TIME_ENTRY_ID).getMessage());

            verify(timeEntryService).getTimeEntry(USER_ID, TIME_ENTRY_ID);
        }
    }

    @Nested
    class CreateTimeEntry {
        private static final String URL = "/api/v1/time-entries";

        @Test
        void returnsCreatedTimeEntry() {
            final TimeEntry createdTimeEntry = createTimeEntry();
            final CreateTimeEntryRequest createTimeEntryRequest = createCreateTimeEntryRequest();
            final CreateTimeEntryRequestDTO createTimeEntryRequestDTO = createCreateTimeEntryRequestDTO();

            when(timeEntryMapper.fromDTO(
                    eq(createTimeEntryRequestDTO),
                    eq(USER_ID)))
                    .thenReturn(createTimeEntryRequest);
            when(timeEntryService.createTimeEntry(createTimeEntryRequest)).thenReturn(createdTimeEntry);
            when(timeEntryMapper.toDTO(eq(createdTimeEntry))).thenReturn(createTimeEntryDTO());

            assertThat(mockMvc.post().uri(URL).with(createPrincipal(USER_ID))
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(CREATE_TIME_ENTRY_JSON_BODY))
                    .hasStatus(201)
                    .bodyJson()
                    .isEqualTo(createTimeEntryDTOJson());

            verify(timeEntryMapper).fromDTO(
                    eq(createTimeEntryRequestDTO),
                    eq(USER_ID));
            verify(timeEntryService).createTimeEntry(createTimeEntryRequest);
        }

        @Test
        void rejectsUnauthenticatedRequest() {
            assertThat(mockMvc.post().uri(URL)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(CREATE_TIME_ENTRY_JSON_BODY))
                    .hasStatus(401);

            verifyNoInteractions(timeEntryMapper, timeEntryService);
        }

        @Test
        void rejectsNullProjectId() {
            final String jsonBody = """
                    {
                        "projectId": null,
                        "isBillable": true,
                        "startTime": "%s"
                    }""".formatted(START_TIME);

            assertThat(mockMvc.post().uri(URL).with(createPrincipal(USER_ID))
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(jsonBody))
                    .hasStatus(400)
                    .bodyJson()
                    .extractingPath("$.error").asString().isEqualTo(TIME_ENTRY_PROJECT_NULL_ERROR_MESSAGE);

            verifyNoInteractions(timeEntryMapper, timeEntryService);
        }

        @Test
        void rejectsNonProvidedProjectId() {
            final String jsonBody = """
                    {
                        "isBillable": true,
                        "startTime": "%s"
                    }""".formatted(START_TIME);

            assertThat(mockMvc.post().uri(URL).with(createPrincipal(USER_ID))
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(jsonBody))
                    .hasStatus(400)
                    .bodyJson()
                    .extractingPath("$.error").asString().isEqualTo(TIME_ENTRY_PROJECT_NULL_ERROR_MESSAGE);

            verifyNoInteractions(timeEntryMapper, timeEntryService);
        }

        @Test
        void rejectsNullBillableStatus() {
            final String jsonBody = """
                    {
                        "projectId": %s,
                        "isBillable": null,
                        "startTime": "%s"
                    }""".formatted(PROJECT_ID, START_TIME);

            assertThat(mockMvc.post().uri(URL).with(createPrincipal(USER_ID))
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(jsonBody))
                    .hasStatus(400)
                    .bodyJson()
                    .extractingPath("$.error").asString().isEqualTo(TIME_ENTRY_BILLABLE_STATUS_NULL_ERROR_MESSAGE);

            verifyNoInteractions(timeEntryMapper, timeEntryService);
        }

        @Test
        void rejectsNonProvidedBillableStatus() {
            final String jsonBody = """
                    {
                        "projectId": %s,
                        "startTime": "%s"
                    }""".formatted(PROJECT_ID, START_TIME);

            assertThat(mockMvc.post().uri(URL).with(createPrincipal(USER_ID))
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(jsonBody))
                    .hasStatus(400)
                    .bodyJson()
                    .extractingPath("$.error").asString().isEqualTo(TIME_ENTRY_BILLABLE_STATUS_NULL_ERROR_MESSAGE);

            verifyNoInteractions(timeEntryMapper, timeEntryService);
        }

        @Test
        void rejectsNullStartTime() {
            final String jsonBody = """
                    {
                        "projectId": %s,
                        "isBillable": true,
                        "startTime": null
                    }""".formatted(PROJECT_ID);

            assertThat(mockMvc.post().uri(URL).with(createPrincipal(USER_ID))
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(jsonBody))
                    .hasStatus(400)
                    .bodyJson()
                    .extractingPath("$.error").asString().isEqualTo(TIME_ENTRY_START_TIME_NULL_ERROR_MESSAGE);

            verifyNoInteractions(timeEntryMapper, timeEntryService);
        }

        @Test
        void rejectsNonProvidedStartTime() {
            final String jsonBody = """
                    {
                        "projectId": %s,
                        "isBillable": true
                    }""".formatted(PROJECT_ID);

            assertThat(mockMvc.post().uri(URL).with(createPrincipal(USER_ID))
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(jsonBody))
                    .hasStatus(400)
                    .bodyJson()
                    .extractingPath("$.error").asString().isEqualTo(TIME_ENTRY_START_TIME_NULL_ERROR_MESSAGE);

            verifyNoInteractions(timeEntryMapper, timeEntryService);
        }
    }

    @Nested
    class PatchTimeEntry {
        private static final String URL = "/api/v1/time-entries/{timeEntryId}";

        @Test
        void patchTimeEntryDescription() {
            final TimeEntry patchedTimeEntry = createTimeEntry();
            final PatchTimeEntryRequestDTO patchTimeEntryRequestDTO = new PatchTimeEntryRequestDTOBuilder()
                    .description("MyDescription")
                    .build();
            final PatchTimeEntryRequest patchTimeEntryRequest = toPatchTimeEntryRequest(patchTimeEntryRequestDTO);

            when(timeEntryMapper.fromDTO(
                    eq(patchTimeEntryRequestDTO),
                    eq(USER_ID)))
                    .thenReturn(patchTimeEntryRequest);
            when(timeEntryService.patchTimeEntry(
                    eq(USER_ID),
                    eq(TIME_ENTRY_ID),
                    eq(patchTimeEntryRequest))).thenReturn(patchedTimeEntry);
            when(timeEntryMapper.toDTO(eq(patchedTimeEntry))).thenReturn(createTimeEntryDTO());

            final String jsonBody = """
                    {
                        "description": "MyDescription"
                    }""";

            assertThat(mockMvc.patch().uri(URL, TIME_ENTRY_ID).with(createPrincipal(USER_ID))
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(jsonBody))
                    .hasStatus(200)
                    .bodyJson()
                    .isEqualTo(createTimeEntryDTOJson());

            verify(timeEntryMapper).fromDTO(
                    eq(patchTimeEntryRequestDTO),
                    eq(USER_ID));
            verify(timeEntryService).patchTimeEntry(
                    eq(USER_ID),
                    eq(TIME_ENTRY_ID),
                    eq(patchTimeEntryRequest));
        }

        @Test
        void patchTimeEntryProject() {
            final TimeEntry patchedTimeEntry = createTimeEntry();
            final PatchTimeEntryRequestDTO patchTimeEntryRequestDTO = new PatchTimeEntryRequestDTOBuilder()
                    .projectId(PROJECT_ID)
                    .build();
            final PatchTimeEntryRequest patchTimeEntryRequest = toPatchTimeEntryRequest(patchTimeEntryRequestDTO);

            when(timeEntryMapper.fromDTO(
                    eq(patchTimeEntryRequestDTO),
                    eq(USER_ID)))
                    .thenReturn(patchTimeEntryRequest);
            when(timeEntryService.patchTimeEntry(
                    eq(USER_ID),
                    eq(TIME_ENTRY_ID),
                    eq(patchTimeEntryRequest))).thenReturn(patchedTimeEntry);
            when(timeEntryMapper.toDTO(eq(patchedTimeEntry))).thenReturn(createTimeEntryDTO());

            final String jsonBody = """
                    {
                        "projectId": %s
                    }""".formatted(PROJECT_ID);

            assertThat(mockMvc.patch().uri(URL, TIME_ENTRY_ID).with(createPrincipal(USER_ID))
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(jsonBody))
                    .hasStatus(200)
                    .bodyJson()
                    .isEqualTo(createTimeEntryDTOJson());

            verify(timeEntryMapper).fromDTO(
                    eq(patchTimeEntryRequestDTO),
                    eq(USER_ID));
            verify(timeEntryService).patchTimeEntry(
                    eq(USER_ID),
                    eq(TIME_ENTRY_ID),
                    eq(patchTimeEntryRequest));
        }

        @Test
        void patchTimeEntryTask() {
            final TimeEntry patchedTimeEntry = createTimeEntry();
            final PatchTimeEntryRequestDTO patchTimeEntryRequestDTO = new PatchTimeEntryRequestDTOBuilder()
                    .taskId(TASK_ID)
                    .build();
            final PatchTimeEntryRequest patchTimeEntryRequest = toPatchTimeEntryRequest(patchTimeEntryRequestDTO);

            when(timeEntryMapper.fromDTO(
                    eq(patchTimeEntryRequestDTO),
                    eq(USER_ID)))
                    .thenReturn(patchTimeEntryRequest);
            when(timeEntryService.patchTimeEntry(
                    eq(USER_ID),
                    eq(TIME_ENTRY_ID),
                    eq(patchTimeEntryRequest))).thenReturn(patchedTimeEntry);
            when(timeEntryMapper.toDTO(eq(patchedTimeEntry))).thenReturn(createTimeEntryDTO());

            final String jsonBody = """
                    {
                        "taskId": %s
                    }""".formatted(TASK_ID);

            assertThat(mockMvc.patch().uri(URL, TIME_ENTRY_ID).with(createPrincipal(USER_ID))
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(jsonBody))
                    .hasStatus(200)
                    .bodyJson()
                    .isEqualTo(createTimeEntryDTOJson());

            verify(timeEntryMapper).fromDTO(
                    eq(patchTimeEntryRequestDTO),
                    eq(USER_ID));
            verify(timeEntryService).patchTimeEntry(
                    eq(USER_ID),
                    eq(TIME_ENTRY_ID),
                    eq(patchTimeEntryRequest));
        }

        @Test
        void patchTimeEntryBillable() {
            final TimeEntry patchedTimeEntry = createTimeEntry();
            final PatchTimeEntryRequestDTO patchTimeEntryRequestDTO = new PatchTimeEntryRequestDTOBuilder()
                    .isBillable(true)
                    .build();
            final PatchTimeEntryRequest patchTimeEntryRequest = toPatchTimeEntryRequest(patchTimeEntryRequestDTO);

            when(timeEntryMapper.fromDTO(
                    eq(patchTimeEntryRequestDTO),
                    eq(USER_ID)))
                    .thenReturn(patchTimeEntryRequest);
            when(timeEntryService.patchTimeEntry(
                    eq(USER_ID),
                    eq(TIME_ENTRY_ID),
                    eq(patchTimeEntryRequest))).thenReturn(patchedTimeEntry);
            when(timeEntryMapper.toDTO(eq(patchedTimeEntry))).thenReturn(createTimeEntryDTO());

            final String jsonBody = """
                    {
                        "isBillable": true
                    }""";

            assertThat(mockMvc.patch().uri(URL, TIME_ENTRY_ID).with(createPrincipal(USER_ID))
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(jsonBody))
                    .hasStatus(200)
                    .bodyJson()
                    .isEqualTo(createTimeEntryDTOJson());

            verify(timeEntryMapper).fromDTO(
                    eq(patchTimeEntryRequestDTO),
                    eq(USER_ID));
            verify(timeEntryService).patchTimeEntry(
                    eq(USER_ID),
                    eq(TIME_ENTRY_ID),
                    eq(patchTimeEntryRequest));
        }

        @Test
        void patchTimeEntryStartTime() {
            final TimeEntry patchedTimeEntry = createTimeEntry();
            final PatchTimeEntryRequestDTO patchTimeEntryRequestDTO = new PatchTimeEntryRequestDTOBuilder()
                    .startTime(START_TIME)
                    .build();
            final PatchTimeEntryRequest patchTimeEntryRequest = toPatchTimeEntryRequest(patchTimeEntryRequestDTO);

            when(timeEntryMapper.fromDTO(
                    eq(patchTimeEntryRequestDTO),
                    eq(USER_ID)))
                    .thenReturn(patchTimeEntryRequest);
            when(timeEntryService.patchTimeEntry(
                    eq(USER_ID),
                    eq(TIME_ENTRY_ID),
                    eq(patchTimeEntryRequest))).thenReturn(patchedTimeEntry);
            when(timeEntryMapper.toDTO(eq(patchedTimeEntry))).thenReturn(createTimeEntryDTO());

            final String jsonBody = """
                    {
                        "startTime": "%s"
                    }""".formatted(START_TIME);

            assertThat(mockMvc.patch().uri(URL, TIME_ENTRY_ID).with(createPrincipal(USER_ID))
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(jsonBody))
                    .hasStatus(200)
                    .bodyJson()
                    .isEqualTo(createTimeEntryDTOJson());

            verify(timeEntryMapper).fromDTO(
                    eq(patchTimeEntryRequestDTO),
                    eq(USER_ID));
            verify(timeEntryService).patchTimeEntry(
                    eq(USER_ID),
                    eq(TIME_ENTRY_ID),
                    eq(patchTimeEntryRequest));
        }

        @Test
        void patchTimeEntryEndTime() {
            final TimeEntry patchedTimeEntry = createTimeEntry();
            final PatchTimeEntryRequestDTO patchTimeEntryRequestDTO = new PatchTimeEntryRequestDTOBuilder()
                    .endTime(END_TIME)
                    .build();
            final PatchTimeEntryRequest patchTimeEntryRequest = toPatchTimeEntryRequest(patchTimeEntryRequestDTO);

            when(timeEntryMapper.fromDTO(
                    eq(patchTimeEntryRequestDTO),
                    eq(USER_ID)))
                    .thenReturn(patchTimeEntryRequest);
            when(timeEntryService.patchTimeEntry(
                    eq(USER_ID),
                    eq(TIME_ENTRY_ID),
                    eq(patchTimeEntryRequest))).thenReturn(patchedTimeEntry);
            when(timeEntryMapper.toDTO(eq(patchedTimeEntry))).thenReturn(createTimeEntryDTO());

            final String jsonBody = """
                    {
                        "endTime": "%s"
                    }""".formatted(END_TIME);

            assertThat(mockMvc.patch().uri(URL, TIME_ENTRY_ID).with(createPrincipal(USER_ID))
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(jsonBody))
                    .hasStatus(200)
                    .bodyJson()
                    .isEqualTo(createTimeEntryDTOJson());

            verify(timeEntryMapper).fromDTO(
                    eq(patchTimeEntryRequestDTO),
                    eq(USER_ID));
            verify(timeEntryService).patchTimeEntry(
                    eq(USER_ID),
                    eq(TIME_ENTRY_ID),
                    eq(patchTimeEntryRequest));
        }

        @Test
        void rejectsUnauthenticatedRequest() {
            assertThat(mockMvc.patch().uri(URL, TIME_ENTRY_ID)).hasStatus(401);

            verifyNoInteractions(timeEntryMapper, timeEntryService);
        }

        @Test
        void rejectsNonExistentTimeEntry() {
            final PatchTimeEntryRequestDTO patchTimeEntryRequestDTO = new PatchTimeEntryRequestDTOBuilder()
                    .description("MyDescription")
                    .build();
            final PatchTimeEntryRequest patchTimeEntryRequest = toPatchTimeEntryRequest(patchTimeEntryRequestDTO);

            when(timeEntryMapper.fromDTO(
                    eq(patchTimeEntryRequestDTO),
                    eq(USER_ID)))
                    .thenReturn(patchTimeEntryRequest);
            when(timeEntryService.patchTimeEntry(
                    eq(USER_ID),
                    eq(TIME_ENTRY_ID),
                    eq(patchTimeEntryRequest))).thenThrow(new TimeEntryNotFoundException(TIME_ENTRY_ID));

            final String jsonBody = """
                    {
                        "description": "MyDescription"
                    }""";

            assertThat(mockMvc.patch().uri(URL, TIME_ENTRY_ID).with(createPrincipal(USER_ID))
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(jsonBody))
                    .hasStatus(404)
                    .bodyJson()
                    .extractingPath("$.error").asString()
                    .isEqualTo(new TimeEntryNotFoundException(TIME_ENTRY_ID).getMessage());

            verify(timeEntryMapper).fromDTO(
                    eq(patchTimeEntryRequestDTO),
                    eq(USER_ID));
            verify(timeEntryService).patchTimeEntry(
                    eq(USER_ID),
                    eq(TIME_ENTRY_ID),
                    eq(patchTimeEntryRequest));
        }

        @Test
        void rejectsNullProjectId() {
            final String jsonBody = """
                    {
                        "projectId": null
                    }""";

            assertThat(mockMvc.patch().uri(URL, TIME_ENTRY_ID).with(createPrincipal(USER_ID))
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(jsonBody))
                    .hasStatus(400)
                    .bodyJson()
                    .extractingPath("$.error").asString().isEqualTo(TIME_ENTRY_PROJECT_NULL_IF_PROVIDED_ERROR_MESSAGE);

            verifyNoInteractions(timeEntryMapper, timeEntryService);
        }

        @Test
        void rejectsNullBillableStatus() {
            final String jsonBody = """
                    {
                        "isBillable": null
                    }""";

            assertThat(mockMvc.patch().uri(URL, TIME_ENTRY_ID).with(createPrincipal(USER_ID))
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(jsonBody))
                    .hasStatus(400)
                    .bodyJson()
                    .extractingPath("$.error").asString()
                    .isEqualTo(TIME_ENTRY_BILLABLE_STATUS_NULL_IF_PROVIDED_ERROR_MESSAGE);

            verifyNoInteractions(timeEntryMapper, timeEntryService);
        }

        @Test
        void rejectsNullStartTime() {
            final String jsonBody = """
                    {
                        "startTime": null
                    }""";

            assertThat(mockMvc.patch().uri(URL, TIME_ENTRY_ID).with(createPrincipal(USER_ID))
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(jsonBody))
                    .hasStatus(400)
                    .bodyJson()
                    .extractingPath("$.error").asString()
                    .isEqualTo(TIME_ENTRY_START_TIME_NULL_IF_PROVIDED_ERROR_MESSAGE);

            verifyNoInteractions(timeEntryMapper, timeEntryService);
        }
    }

    @Nested
    class DeleteTimeEntry {
        private static final String URL = "/api/v1/time-entries/{timeEntryId}";

        @Test
        void deletesTimeEntry() {
            assertThat(mockMvc.delete().uri(URL, TIME_ENTRY_ID).with(createPrincipal(USER_ID)))
                    .hasStatus(204);

            verify(timeEntryService).deleteTimeEntry(USER_ID, TIME_ENTRY_ID);
        }

        @Test
        void rejectsUnauthenticatedRequest() {
            assertThat(mockMvc.delete().uri(URL, TIME_ENTRY_ID)).hasStatus(401);

            verifyNoInteractions(timeEntryService);
        }

        @Test
        void rejectsNonExistentTimeEntry() {
            doThrow(new TimeEntryNotFoundException(TIME_ENTRY_ID)).when(timeEntryService)
                    .deleteTimeEntry(USER_ID, TIME_ENTRY_ID);

            assertThat(mockMvc.delete().uri(URL, TIME_ENTRY_ID).with(createPrincipal(USER_ID)))
                    .hasStatus(404)
                    .bodyJson()
                    .extractingPath("$.error").asString()
                    .isEqualTo(new TimeEntryNotFoundException(TIME_ENTRY_ID).getMessage());

            verify(timeEntryService).deleteTimeEntry(USER_ID, TIME_ENTRY_ID);
        }
    }

    private static TimeEntry createTimeEntry() {
        final TimeEntry timeEntry = new TimeEntry(
                createUser(USER_ID),
                createProject(),
                createTask(),
                "MyDescription",
                true,
                START_TIME,
                END_TIME);
        ReflectionTestUtils.setField(timeEntry, "id", TIME_ENTRY_ID);
        ReflectionTestUtils.setField(timeEntry, "createdAt", CREATED_AT);

        return timeEntry;
    }

    private static TimeEntryDTO createTimeEntryDTO() {
        return new TimeEntryDTO(
                TIME_ENTRY_ID,
                USER_ID,
                createProjectDTO(),
                createTaskDTO(),
                "MyDescription",
                true,
                START_TIME,
                END_TIME,
                CREATED_AT);
    }

    private static String createTimeEntryDTOJson() {
        return """
                {
                    "id": %s,
                    "userId": %s,
                    "project": %s,
                    "task": %s,
                    "description": "MyDescription",
                    "isBillable": true,
                    "startTime": "%s",
                    "endTime": "%s",
                    "createdAt": "%s"
                }""".formatted(
                TIME_ENTRY_ID,
                USER_ID,
                createProjectDTOJson(),
                createTaskDTOJson(),
                START_TIME,
                END_TIME,
                CREATED_AT);
    }

    private static TimeEntryPage createTimeEntryPage() {
        return new TimeEntryPage(
                List.of(createTimeEntry()),
                null,
                CURSOR,
                null,
                0,
                50,
                1L,
                1,
                true,
                true);
    }

    private static TimeEntryPage createEmptyTimeEntryPage() {
        return new TimeEntryPage(
                List.of(),
                null,
                CURSOR,
                null,
                0,
                50,
                0L,
                0,
                true,
                true);
    }

    private static TimeEntryPageDTO createTimeEntryPageDTO() {
        return new TimeEntryPageDTO(
                List.of(createTimeEntryDTO()),
                null,
                CURSOR,
                null,
                0,
                50,
                1L,
                1,
                true,
                true);
    }

    private static TimeEntryPageDTO createEmptyTimeEntryPageDTO() {
        return new TimeEntryPageDTO(
                List.of(),
                null,
                CURSOR,
                null,
                0,
                50,
                0L,
                0,
                true,
                true);
    }

    private static String createTimeEntryPageDTOJson() {
        return createTimeEntryPageDTOJson("[%s]".formatted(createTimeEntryDTOJson()), 1L, 1);
    }

    private static String createEmptyTimeEntryPageDTOJson() {
        return createTimeEntryPageDTOJson("[]", 0L, 0);
    }

    private static String createTimeEntryPageDTOJson(String contentJson, long totalElements, int totalPages) {
        return """
                {
                    "content": %s,
                    "previousCursor": null,
                    "currentCursor": "%s",
                    "nextCursor": null,
                    "page": 0,
                    "size": 50,
                    "totalElements": %s,
                    "totalPages": %s,
                    "first": true,
                    "last": true
                }""".formatted(contentJson, CURSOR, totalElements, totalPages);
    }

    private static CreateTimeEntryRequest createCreateTimeEntryRequest() {
        return new CreateTimeEntryRequest(
                createUser(USER_ID),
                createProject(),
                createTask(),
                "MyDescription",
                true,
                START_TIME,
                END_TIME);
    }

    private static CreateTimeEntryRequestDTO createCreateTimeEntryRequestDTO() {
        return new CreateTimeEntryRequestDTO(
                PROJECT_ID,
                TASK_ID,
                "MyDescription",
                true,
                START_TIME,
                END_TIME);
    }

    private static PatchTimeEntryRequest toPatchTimeEntryRequest(PatchTimeEntryRequestDTO patchTimeEntryRequestDTO) {
        return new PatchTimeEntryRequest(
                patchTimeEntryRequestDTO.projectId().map(id -> createProject()),
                patchTimeEntryRequestDTO.taskId().map(id -> createTask()),
                patchTimeEntryRequestDTO.description(),
                patchTimeEntryRequestDTO.isBillable(),
                patchTimeEntryRequestDTO.startTime(),
                patchTimeEntryRequestDTO.endTime());
    }

    private static final class PatchTimeEntryRequestDTOBuilder {
        private JsonNullable<Long> projectId = JsonNullable.undefined();
        private JsonNullable<Long> taskId = JsonNullable.undefined();
        private JsonNullable<String> description = JsonNullable.undefined();
        private JsonNullable<Boolean> isBillable = JsonNullable.undefined();
        private JsonNullable<Instant> startTime = JsonNullable.undefined();
        private JsonNullable<Instant> endTime = JsonNullable.undefined();

        private PatchTimeEntryRequestDTOBuilder projectId(Long projectId) {
            this.projectId = JsonNullable.of(projectId);
            return this;
        }

        private PatchTimeEntryRequestDTOBuilder taskId(Long taskId) {
            this.taskId = JsonNullable.of(taskId);
            return this;
        }

        private PatchTimeEntryRequestDTOBuilder description(String description) {
            this.description = JsonNullable.of(description);
            return this;
        }

        private PatchTimeEntryRequestDTOBuilder isBillable(Boolean isBillable) {
            this.isBillable = JsonNullable.of(isBillable);
            return this;
        }

        private PatchTimeEntryRequestDTOBuilder startTime(Instant startTime) {
            this.startTime = JsonNullable.of(startTime);
            return this;
        }

        private PatchTimeEntryRequestDTOBuilder endTime(Instant endTime) {
            this.endTime = JsonNullable.of(endTime);
            return this;
        }

        private PatchTimeEntryRequestDTO build() {
            return new PatchTimeEntryRequestDTO(
                    projectId,
                    taskId,
                    description,
                    isBillable,
                    startTime,
                    endTime);
        }
    }

    private static Project createProject() {
        final Project project = new Project("MyProject", createUser(USER_ID), null, null);
        ReflectionTestUtils.setField(project, "id", PROJECT_ID);
        ReflectionTestUtils.setField(project, "createdAt", CREATED_AT);

        return project;
    }

    private static ProjectDTO createProjectDTO() {
        return new ProjectDTO(
                PROJECT_ID,
                "MyProject",
                USER_ID,
                null,
                List.of(),
                null,
                CREATED_AT);
    }

    private static String createProjectDTOJson() {
        return """
                {
                    "id": %s,
                    "name": "MyProject",
                    "userId": %s,
                    "hourlyRate": null,
                    "tasks": [],
                    "client": null,
                    "createdAt": "%s"
                }""".formatted(PROJECT_ID, USER_ID, CREATED_AT);
    }

    private static Task createTask() {
        final Task task = new Task("MyTask", createUser(USER_ID), createProject());
        ReflectionTestUtils.setField(task, "id", TASK_ID);
        ReflectionTestUtils.setField(task, "createdAt", CREATED_AT);

        return task;
    }

    private static TaskDTO createTaskDTO() {
        return new TaskDTO(TASK_ID, "MyTask", USER_ID, PROJECT_ID, CREATED_AT);
    }

    private static String createTaskDTOJson() {
        return """
                {
                    "id": %s,
                    "name": "MyTask",
                    "userId": %s,
                    "projectId": %s,
                    "createdAt": "%s"
                }""".formatted(TASK_ID, USER_ID, PROJECT_ID, CREATED_AT);
    }
}
