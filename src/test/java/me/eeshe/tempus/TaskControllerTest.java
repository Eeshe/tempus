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
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.util.ReflectionTestUtils;

import me.eeshe.tempus.controller.TaskController;
import me.eeshe.tempus.dto.CreateTaskRequestDTO;
import me.eeshe.tempus.dto.PatchTaskRequestDTO;
import me.eeshe.tempus.dto.TaskDTO;
import me.eeshe.tempus.entity.Project;
import me.eeshe.tempus.entity.Task;
import me.eeshe.tempus.exception.ProjectNotFoundException;
import me.eeshe.tempus.exception.TaskNotFoundException;
import me.eeshe.tempus.exception.UserProjectTaskAlreadyExistsException;
import me.eeshe.tempus.mapper.TaskMapper;
import me.eeshe.tempus.request.CreateTaskRequest;
import me.eeshe.tempus.request.PatchTaskRequest;
import me.eeshe.tempus.service.TaskService;
import me.eeshe.tempus.support.ControllerTestBase;

@WebMvcTest(TaskController.class)
public class TaskControllerTest extends ControllerTestBase {
    private static final long USER_ID = 1L;
    private static final long PROJECT_ID = 20L;
    private static final long TASK_ID = 300L;

    private static final Instant CREATED_AT = Instant.parse("2026-01-01T10:00:00Z");

    private static final String TASK_NAME_NULL_OR_EMPTY_ERROR_MESSAGE = "Task name can't be null or empty";
    private static final String TASK_NAME_EMPTY_IF_PROVIDED_ERROR_MESSAGE = "Task name can't be empty if provided";
    private static final String TASK_PROJECT_NULL_ERROR_MESSAGE = "Task project can't be null";

    private static final String CREATE_TASK_JSON_BODY = """
            {
                "name": "MyTask",
                "projectId": %s
            }""".formatted(PROJECT_ID);

    private static final String PATCH_TASK_JSON_BODY = """
            {
                "name": "MyTask"
            }""";

    @MockitoBean
    private TaskService taskService;

    @MockitoBean
    private TaskMapper taskMapper;

    @Nested
    class GetTasks {
        private static final String URL = "/api/v1/tasks";

        @Test
        void returnsAuthenticatedUserTasks() {
            final Task task = createTask();

            when(taskService.listTasks(USER_ID)).thenReturn(List.of(task));
            when(taskMapper.toDTO(task)).thenReturn(createTaskDTO());

            assertThat(mockMvc.get().uri(URL).with(createPrincipal(USER_ID)))
                    .hasStatus(200)
                    .bodyJson()
                    .isEqualTo("[%s]".formatted(createTaskDTOJson()));

            verify(taskService).listTasks(USER_ID);
        }

        @Test
        void returnsAuthenticatedEmptyUserTasks() {
            assertThat(mockMvc.get().uri(URL).with(createPrincipal(USER_ID)))
                    .hasStatus(200)
                    .bodyJson()
                    .isEqualTo("[]");

            verify(taskService).listTasks(USER_ID);
        }

        @Test
        void rejectsUnauthenticatedRequest() {
            assertThat(mockMvc.get().uri(URL)).hasStatus(401);

            verifyNoInteractions(taskService);
        }
    }

    @Nested
    class GetTask {
        private static final String URL = "/api/v1/tasks/{taskId}";

        @Test
        void returnsAuthenticatedUserTask() {
            final Task task = createTask();

            when(taskService.getTask(USER_ID, TASK_ID)).thenReturn(task);
            when(taskMapper.toDTO(task)).thenReturn(createTaskDTO());

            assertThat(mockMvc.get().uri(URL, TASK_ID).with(createPrincipal(USER_ID)))
                    .hasStatus(200)
                    .bodyJson()
                    .isEqualTo(createTaskDTOJson());

            verify(taskService).getTask(USER_ID, TASK_ID);
        }

        @Test
        void rejectsUnauthenticatedRequest() {
            assertThat(mockMvc.get().uri(URL, TASK_ID)).hasStatus(401);

            verifyNoInteractions(taskService);
        }

        @Test
        void rejectsTaskNotFound() {
            when(taskService.getTask(USER_ID, TASK_ID)).thenThrow(new TaskNotFoundException(TASK_ID));

            assertThat(mockMvc.get().uri(URL, TASK_ID).with(createPrincipal(USER_ID)))
                    .hasStatus(404)
                    .bodyJson()
                    .extractingPath("$.error").asString()
                    .isEqualTo(new TaskNotFoundException(TASK_ID).getMessage());

            verify(taskService).getTask(USER_ID, TASK_ID);
        }
    }

    @Nested
    class CreateTask {
        private static final String URL = "/api/v1/tasks";

        @Test
        void returnsCreatedTask() {
            final Task createdTask = createTask();
            final CreateTaskRequest createTaskRequest = createTaskRequest();
            final CreateTaskRequestDTO createTaskRequestDTO = createCreateTaskRequestDTO();

            when(taskMapper.fromDTO(
                    eq(createTaskRequestDTO),
                    eq(USER_ID)))
                    .thenReturn(createTaskRequest);
            when(taskService.createTask(createTaskRequest)).thenReturn(createdTask);
            when(taskMapper.toDTO(eq(createdTask))).thenReturn(createTaskDTO());

            assertThat(mockMvc.post().uri(URL).with(createPrincipal(USER_ID))
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(CREATE_TASK_JSON_BODY))
                    .hasStatus(201)
                    .bodyJson()
                    .isEqualTo(createTaskDTOJson());

            verify(taskMapper).fromDTO(
                    eq(createTaskRequestDTO),
                    eq(USER_ID));
            verify(taskService).createTask(createTaskRequest);
        }

        @Test
        void rejectsUnauthenticatedRequest() {
            assertThat(mockMvc.post().uri(URL)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(CREATE_TASK_JSON_BODY))
                    .hasStatus(401);

            verifyNoInteractions(taskMapper, taskService);
        }

        @Test
        void rejectsAlreadyExistentUserTask() {
            final CreateTaskRequest createTaskRequest = createTaskRequest();
            final CreateTaskRequestDTO createTaskRequestDTO = createCreateTaskRequestDTO();

            when(taskMapper.fromDTO(
                    eq(createTaskRequestDTO),
                    eq(USER_ID)))
                    .thenReturn(createTaskRequest);
            when(taskService.createTask(createTaskRequest))
                    .thenThrow(new UserProjectTaskAlreadyExistsException(
                            USER_ID,
                            "MyTask",
                            "MyProject"));

            assertThat(mockMvc.post().uri(URL).with(createPrincipal(USER_ID))
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(CREATE_TASK_JSON_BODY))
                    .hasStatus(400)
                    .bodyJson()
                    .extractingPath("$.error").asString()
                    .isEqualTo(new UserProjectTaskAlreadyExistsException(USER_ID, "MyTask", "MyProject").getMessage());

            verify(taskMapper).fromDTO(
                    eq(createTaskRequestDTO),
                    eq(USER_ID));
            verify(taskService).createTask(createTaskRequest);
        }

        @Test
        void rejectsEmptyTaskName() {
            final String jsonBody = """
                    {
                        "name": "",
                        "projectId": %s
                    }""".formatted(PROJECT_ID);

            assertThat(mockMvc.post().uri(URL).with(createPrincipal(USER_ID))
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(jsonBody))
                    .hasStatus(400)
                    .bodyJson()
                    .extractingPath("$.error").asString().isEqualTo(TASK_NAME_NULL_OR_EMPTY_ERROR_MESSAGE);

            verifyNoInteractions(taskMapper, taskService);
        }

        @Test
        void rejectsBlankTaskName() {
            final String jsonBody = """
                    {
                        "name": "  ",
                        "projectId": %s
                    }""".formatted(PROJECT_ID);

            assertThat(mockMvc.post().uri(URL).with(createPrincipal(USER_ID))
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(jsonBody))
                    .hasStatus(400)
                    .bodyJson()
                    .extractingPath("$.error").asString().isEqualTo(TASK_NAME_NULL_OR_EMPTY_ERROR_MESSAGE);

            verifyNoInteractions(taskMapper, taskService);
        }

        @Test
        void rejectsNonProvidedTaskName() {
            final String jsonBody = """
                    {
                        "projectId": %s
                    }""".formatted(PROJECT_ID);

            assertThat(mockMvc.post().uri(URL).with(createPrincipal(USER_ID))
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(jsonBody))
                    .hasStatus(400)
                    .bodyJson()
                    .extractingPath("$.error").asString().isEqualTo(TASK_NAME_NULL_OR_EMPTY_ERROR_MESSAGE);

            verifyNoInteractions(taskMapper, taskService);
        }

        @Test
        void rejectsNonExistentProject() {
            final CreateTaskRequest createTaskRequest = createTaskRequest();
            final CreateTaskRequestDTO createTaskRequestDTO = createCreateTaskRequestDTO();

            when(taskMapper.fromDTO(
                    eq(createTaskRequestDTO),
                    eq(USER_ID)))
                    .thenReturn(createTaskRequest);
            when(taskService.createTask(createTaskRequest))
                    .thenThrow(new ProjectNotFoundException(PROJECT_ID));

            assertThat(mockMvc.post().uri(URL).with(createPrincipal(USER_ID))
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(CREATE_TASK_JSON_BODY))
                    .hasStatus(404)
                    .bodyJson()
                    .extractingPath("$.error").asString()
                    .isEqualTo(new ProjectNotFoundException(PROJECT_ID).getMessage());

            verify(taskMapper).fromDTO(
                    eq(createTaskRequestDTO),
                    eq(USER_ID));
            verify(taskService).createTask(createTaskRequest);
        }

        @Test
        void rejectsEmptyProjectId() {
            final String jsonBody = """
                    {
                        "name": "MyTask",
                        "projectId": ""
                    }""";

            assertThat(mockMvc.post().uri(URL).with(createPrincipal(USER_ID))
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(jsonBody))
                    .hasStatus(400)
                    .bodyJson()
                    .extractingPath("$.error").asString().isEqualTo(TASK_PROJECT_NULL_ERROR_MESSAGE);

            verifyNoInteractions(taskMapper, taskService);
        }

        @Test
        void rejectsNullProjectId() {
            final String jsonBody = """
                    {
                        "name": "MyTask",
                        "projectId": "null"
                    }""";

            assertThat(mockMvc.post().uri(URL).with(createPrincipal(USER_ID))
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(jsonBody))
                    .hasStatus(400)
                    .bodyJson()
                    .extractingPath("$.error").asString().isEqualTo(TASK_PROJECT_NULL_ERROR_MESSAGE);

            verifyNoInteractions(taskMapper, taskService);
        }

        @Test
        void rejectsNonProvidedProjectId() {
            final String jsonBody = """
                    {
                        "name": "MyTask"
                    }""";

            assertThat(mockMvc.post().uri(URL).with(createPrincipal(USER_ID))
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(jsonBody))
                    .hasStatus(400)
                    .bodyJson()
                    .extractingPath("$.error").asString().isEqualTo(TASK_PROJECT_NULL_ERROR_MESSAGE);

            verifyNoInteractions(taskMapper, taskService);
        }
    }

    @Nested
    class PatchTask {
        private static final String URL = "/api/v1/tasks/{taskId}";

        @Test
        void patchTaskName() {
            final Task patchedTask = createTask();
            final PatchTaskRequest patchTaskRequest = createPatchTaskRequest();
            final PatchTaskRequestDTO patchTaskRequestDTO = createPatchTaskRequestDTO();

            when(taskMapper.fromDTO(
                    eq(patchTaskRequestDTO),
                    eq(USER_ID)))
                    .thenReturn(patchTaskRequest);
            when(taskService.patchTask(
                    eq(USER_ID),
                    eq(TASK_ID),
                    eq(patchTaskRequest))).thenReturn(patchedTask);
            when(taskMapper.toDTO(eq(patchedTask))).thenReturn(createTaskDTO());

            assertThat(mockMvc.patch().uri(URL, TASK_ID).with(createPrincipal(USER_ID))
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(PATCH_TASK_JSON_BODY))
                    .hasStatus(200)
                    .bodyJson()
                    .isEqualTo(createTaskDTOJson());

            verify(taskMapper).fromDTO(
                    eq(patchTaskRequestDTO),
                    eq(USER_ID));
            verify(taskService).patchTask(
                    eq(USER_ID),
                    eq(TASK_ID),
                    eq(patchTaskRequest));
        }

        @Test
        void rejectsUnauthenticatedRequest() {
            assertThat(mockMvc.patch().uri(URL, TASK_ID)).hasStatus(401);

            verifyNoInteractions(taskMapper, taskService);
        }

        @Test
        void rejectsNonExistentTask() {
            final PatchTaskRequest patchTaskRequest = createPatchTaskRequest();
            final PatchTaskRequestDTO patchTaskRequestDTO = createPatchTaskRequestDTO();

            when(taskMapper.fromDTO(
                    eq(patchTaskRequestDTO),
                    eq(USER_ID)))
                    .thenReturn(patchTaskRequest);
            when(taskService.patchTask(
                    eq(USER_ID),
                    eq(TASK_ID),
                    eq(patchTaskRequest))).thenThrow(new TaskNotFoundException(TASK_ID));

            assertThat(mockMvc.patch().uri(URL, TASK_ID).with(createPrincipal(USER_ID))
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(PATCH_TASK_JSON_BODY))
                    .hasStatus(404)
                    .bodyJson()
                    .extractingPath("$.error").asString()
                    .isEqualTo(new TaskNotFoundException(TASK_ID).getMessage());

            verify(taskMapper).fromDTO(
                    eq(patchTaskRequestDTO),
                    eq(USER_ID));
            verify(taskService).patchTask(
                    eq(USER_ID),
                    eq(TASK_ID),
                    eq(patchTaskRequest));
        }

        @Test
        void rejectsEmptyName() {
            final String jsonBody = """
                    {
                        "name": ""
                    }""";

            assertThat(mockMvc.patch().uri(URL, TASK_ID).with(createPrincipal(USER_ID))
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(jsonBody))
                    .hasStatus(400)
                    .bodyJson()
                    .extractingPath("$.error").asString().isEqualTo(TASK_NAME_EMPTY_IF_PROVIDED_ERROR_MESSAGE);

            verifyNoInteractions(taskMapper, taskService);
        }

        @Test
        void rejectsBlankName() {
            final String jsonBody = """
                    {
                        "name": "  "
                    }""";

            assertThat(mockMvc.patch().uri(URL, TASK_ID).with(createPrincipal(USER_ID))
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(jsonBody))
                    .hasStatus(400)
                    .bodyJson()
                    .extractingPath("$.error").asString().isEqualTo(TASK_NAME_EMPTY_IF_PROVIDED_ERROR_MESSAGE);

            verifyNoInteractions(taskMapper, taskService);
        }
    }

    @Nested
    class DeleteTask {
        private static final String URL = "/api/v1/tasks/{taskId}";

        @Test
        void deletesTask() {
            assertThat(mockMvc.delete().uri(URL, TASK_ID).with(createPrincipal(USER_ID)))
                    .hasStatus(204);

            verify(taskService).deleteTask(USER_ID, TASK_ID);
        }

        @Test
        void rejectsUnauthenticatedRequest() {
            assertThat(mockMvc.delete().uri(URL, TASK_ID)).hasStatus(401);

            verifyNoInteractions(taskService);
        }

        @Test
        void rejectsNonExistentTask() {
            doThrow(new TaskNotFoundException(TASK_ID)).when(taskService).deleteTask(USER_ID, TASK_ID);

            assertThat(mockMvc.delete().uri(URL, TASK_ID).with(createPrincipal(USER_ID)))
                    .hasStatus(404)
                    .bodyJson()
                    .extractingPath("$.error").asString()
                    .isEqualTo(new TaskNotFoundException(TASK_ID).getMessage());

            verify(taskService).deleteTask(USER_ID, TASK_ID);
        }
    }

    private static Task createTask() {
        final Task task = new Task("MyTask", createUser(USER_ID), createProject());
        ReflectionTestUtils.setField(task, "id", TASK_ID);
        ReflectionTestUtils.setField(task, "createdAt", CREATED_AT);

        return task;
    }

    private static TaskDTO createTaskDTO() {
        return new TaskDTO(
                TASK_ID,
                "MyTask",
                USER_ID,
                PROJECT_ID,
                CREATED_AT);
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

    private static CreateTaskRequest createTaskRequest() {
        return new CreateTaskRequest("MyTask", createUser(USER_ID), createProject());
    }

    private static CreateTaskRequestDTO createCreateTaskRequestDTO() {
        return new CreateTaskRequestDTO("MyTask", PROJECT_ID);
    }

    private static PatchTaskRequest createPatchTaskRequest() {
        return new PatchTaskRequest("MyTask");
    }

    private static PatchTaskRequestDTO createPatchTaskRequestDTO() {
        return new PatchTaskRequestDTO("MyTask");
    }

    private static Project createProject() {
        final Project project = new Project("MyProject", createUser(USER_ID), null, null);
        ReflectionTestUtils.setField(project, "id", PROJECT_ID);

        return project;
    }
}
