package me.eeshe.tempus;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.List;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.openapitools.jackson.nullable.JsonNullable;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import me.eeshe.tempus.controller.ProjectController;
import me.eeshe.tempus.dto.CreateProjectRequestDTO;
import me.eeshe.tempus.dto.PatchProjectRequestDTO;
import me.eeshe.tempus.entity.Project;
import me.eeshe.tempus.exception.ClientNotFoundException;
import me.eeshe.tempus.exception.ProjectNotFoundException;
import me.eeshe.tempus.exception.UserProjectAlreadyExistsException;
import me.eeshe.tempus.mapper.ProjectMapper;
import me.eeshe.tempus.request.CreateProjectRequest;
import me.eeshe.tempus.request.PatchProjectRequest;
import me.eeshe.tempus.service.ProjectService;
import me.eeshe.tempus.support.ControllerTestBase;

@WebMvcTest(ProjectController.class)
public class ProjectControllerTest extends ControllerTestBase {
    private static final String CREATE_PROJECT_JSON_BODY = """
            {
                "name": "MyProject",
                "clientId": null
            }""";

    private static final String PATCH_PROJECT_JSON_BODY = """
            {
                "name": "MyProject"
            }""";

    @MockitoBean
    private ProjectService projectService;

    @MockitoBean
    private ProjectMapper projectMapper;

    @Nested
    class ListProjects {
        private static final String URL = "/api/v1/projects";

        @Test
        void returnsAuthenticatedUserProjects() {
            final Project project = createProject();

            when(projectService.listProjects(USER_ID)).thenReturn(List.of(project));
            when(projectMapper.toDTO(project)).thenReturn(createProjectDTO());

            assertThat(mockMvc.get().uri(URL).with(createPrincipal(USER_ID)))
                    .hasStatus(200)
                    .bodyJson()
                    .isEqualTo("[%s]".formatted(createProjectDTOJson()));

            verify(projectService).listProjects(USER_ID);
        }

        @Test
        void returnsAuthenticatedUserMultipleProjects() {
            final Project firstProject = createProject();
            final Project secondProject = createSecondProject();

            when(projectService.listProjects(USER_ID)).thenReturn(List.of(firstProject, secondProject));
            when(projectMapper.toDTO(firstProject)).thenReturn(createProjectDTO());
            when(projectMapper.toDTO(secondProject)).thenReturn(createSecondProjectDTO());

            assertThat(mockMvc.get().uri(URL).with(createPrincipal(USER_ID)))
                    .hasStatus(200)
                    .bodyJson()
                    .isEqualTo("[%s,%s]".formatted(createProjectDTOJson(), createSecondProjectDTOJson()));

            verify(projectService).listProjects(USER_ID);
        }

        @Test
        void returnsAuthenticatedEmptyUserProjects() {
            assertThat(mockMvc.get().uri(URL).with(createPrincipal(USER_ID)))
                    .hasStatus(200)
                    .bodyJson()
                    .isEqualTo("[]");

            verify(projectService).listProjects(USER_ID);
        }

        @Test
        void rejectsUnauthenticatedRequest() {
            assertThat(mockMvc.get().uri(URL)).hasStatus(401);

            verifyNoInteractions(projectService);
        }
    }

    @Nested
    class GetProject {
        private static final String URL = "/api/v1/projects/{projectId}";

        @Test
        void returnsAuthenticatedUserProject() {
            final Project project = createProject();

            when(projectService.getProject(USER_ID, PROJECT_ID)).thenReturn(project);
            when(projectMapper.toDTO(project)).thenReturn(createProjectDTO());

            assertThat(mockMvc.get().uri(URL, PROJECT_ID).with(createPrincipal(USER_ID)))
                    .hasStatus(200)
                    .bodyJson()
                    .isEqualTo(createProjectDTOJson());

            verify(projectService).getProject(USER_ID, PROJECT_ID);
        }

        @Test
        void rejectsUnauthenticatedRequest() {
            assertThat(mockMvc.get().uri(URL, PROJECT_ID)).hasStatus(401);

            verifyNoInteractions(projectService);
        }

        @Test
        void rejectsNonExistentProject() {
            final ProjectNotFoundException exception = new ProjectNotFoundException(PROJECT_ID);
            when(projectService.getProject(USER_ID, PROJECT_ID)).thenThrow(exception);

            assertThat(mockMvc.get().uri(URL, PROJECT_ID).with(createPrincipal(USER_ID)))
                    .hasStatus(404)
                    .bodyJson()
                    .extractingPath("$.error").asString()
                    .isEqualTo(exception.getMessage());

            verify(projectService).getProject(USER_ID, PROJECT_ID);
        }
    }

    @Nested
    class CreateProject {
        private static final String URL = "/api/v1/projects";

        @Test
        void returnsCreatedProject() {
            final Project createdProject = createProject();
            final CreateProjectRequest createProjectRequest = createProjectRequest();
            final CreateProjectRequestDTO createProjectRequestDTO = createCreateProjectRequestDTO();

            when(projectMapper.fromDTO(
                    eq(createProjectRequestDTO),
                    eq(USER_ID)))
                    .thenReturn(createProjectRequest);
            when(projectService.createProject(createProjectRequest)).thenReturn(createdProject);
            when(projectMapper.toDTO(eq(createdProject))).thenReturn(createProjectDTO());

            assertThat(mockMvc.post().uri(URL).with(createPrincipal(USER_ID))
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(CREATE_PROJECT_JSON_BODY))
                    .hasStatus(201)
                    .bodyJson()
                    .isEqualTo(createProjectDTOJson());

            verify(projectMapper).fromDTO(
                    eq(createProjectRequestDTO),
                    eq(USER_ID));
            verify(projectService).createProject(createProjectRequest);
        }

        @Test
        void rejectsUnauthenticatedRequest() {
            assertThat(mockMvc.post().uri(URL)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(CREATE_PROJECT_JSON_BODY))
                    .hasStatus(401);

            verifyNoInteractions(projectMapper, projectService);
        }

        @Test
        void rejectsAlreadyExistentUserProject() {
            final CreateProjectRequest createProjectRequest = createProjectRequest();
            final CreateProjectRequestDTO createProjectRequestDTO = createCreateProjectRequestDTO();
            final UserProjectAlreadyExistsException exception = new UserProjectAlreadyExistsException(USER_ID,
                    "MyProject");

            when(projectMapper.fromDTO(
                    eq(createProjectRequestDTO),
                    eq(USER_ID)))
                    .thenReturn(createProjectRequest);
            when(projectService.createProject(createProjectRequest))
                    .thenThrow(exception);

            assertThat(mockMvc.post().uri(URL).with(createPrincipal(USER_ID))
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(CREATE_PROJECT_JSON_BODY))
                    .hasStatus(400)
                    .bodyJson()
                    .extractingPath("$.error").asString()
                    .isEqualTo(exception.getMessage());

            verify(projectMapper).fromDTO(
                    eq(createProjectRequestDTO),
                    eq(USER_ID));
            verify(projectService).createProject(createProjectRequest);
        }

        @Test
        void rejectsNonExistentClient() {
            final CreateProjectRequestDTO createProjectRequestDTO = new CreateProjectRequestDTO(
                    "MyProject",
                    CLIENT_ID,
                    null);
            final ClientNotFoundException exception = new ClientNotFoundException(CLIENT_ID);

            when(projectMapper.fromDTO(
                    eq(createProjectRequestDTO),
                    eq(USER_ID)))
                    .thenThrow(exception);

            final String jsonBody = """
                    {
                        "name": "MyProject",
                        "clientId": %s
                    }""".formatted(CLIENT_ID);

            assertThat(mockMvc.post().uri(URL).with(createPrincipal(USER_ID))
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(jsonBody))
                    .hasStatus(404)
                    .bodyJson()
                    .extractingPath("$.error").asString()
                    .isEqualTo(exception.getMessage());

            verify(projectMapper).fromDTO(
                    eq(createProjectRequestDTO),
                    eq(USER_ID));
            verifyNoInteractions(projectService);
        }

        @Test
        void rejectsEmptyProjectName() {
            final String jsonBody = """
                    {
                        "name": "",
                        "clientId": null
                    }""";

            assertThat(mockMvc.post().uri(URL).with(createPrincipal(USER_ID))
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(jsonBody))
                    .hasStatus(400)
                    .bodyJson()
                    .extractingPath("$.error").asString().isEqualTo(CreateProjectRequestDTO.ERROR_MESSAGE_EMPTY_NAME);

            verifyNoInteractions(projectMapper, projectService);
        }

        @Test
        void rejectsBlankProjectName() {
            final String jsonBody = """
                    {
                        "name": "  ",
                        "clientId": null
                    }""";

            assertThat(mockMvc.post().uri(URL).with(createPrincipal(USER_ID))
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(jsonBody))
                    .hasStatus(400)
                    .bodyJson()
                    .extractingPath("$.error").asString().isEqualTo(CreateProjectRequestDTO.ERROR_MESSAGE_EMPTY_NAME);

            verifyNoInteractions(projectMapper, projectService);
        }

        @Test
        void rejectsNonProvidedProjectName() {
            final String jsonBody = """
                    {
                        "clientId": null
                    }""";

            assertThat(mockMvc.post().uri(URL).with(createPrincipal(USER_ID))
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(jsonBody))
                    .hasStatus(400)
                    .bodyJson()
                    .extractingPath("$.error").asString().isEqualTo(CreateProjectRequestDTO.ERROR_MESSAGE_EMPTY_NAME);

            verifyNoInteractions(projectMapper, projectService);
        }

        @Test
        void rejectsNegativeHourlyRate() {
            final String jsonBody = """
                    {
                        "name": "MyProject",
                        "hourlyRate": -1
                    }""";

            assertThat(mockMvc.post().uri(URL).with(createPrincipal(USER_ID))
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(jsonBody))
                    .hasStatus(400)
                    .bodyJson()
                    .extractingPath("$.error").asString()
                    .isEqualTo(CreateProjectRequestDTO.ERROR_MESSAGE_NEGATIVE_RATE);

            verifyNoInteractions(projectMapper, projectService);
        }
    }

    @Nested
    class PatchProject {
        private static final String URL = "/api/v1/projects/{projectId}";

        @Test
        void patchProjectName() {
            final Project patchedProject = createProject();
            final PatchProjectRequest patchProjectRequest = createPatchProjectRequest();
            final PatchProjectRequestDTO patchProjectRequestDTO = createPatchProjectRequestDTO();

            when(projectMapper.fromDTO(
                    eq(patchProjectRequestDTO),
                    eq(USER_ID)))
                    .thenReturn(patchProjectRequest);
            when(projectService.patchProject(
                    eq(USER_ID),
                    eq(PROJECT_ID),
                    eq(patchProjectRequest))).thenReturn(patchedProject);
            when(projectMapper.toDTO(eq(patchedProject))).thenReturn(createProjectDTO());

            assertThat(mockMvc.patch().uri(URL, PROJECT_ID).with(createPrincipal(USER_ID))
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(PATCH_PROJECT_JSON_BODY))
                    .hasStatus(200)
                    .bodyJson()
                    .isEqualTo(createProjectDTOJson());

            verify(projectMapper).fromDTO(
                    eq(patchProjectRequestDTO),
                    eq(USER_ID));
            verify(projectService).patchProject(
                    eq(USER_ID),
                    eq(PROJECT_ID),
                    eq(patchProjectRequest));
        }

        @Test
        void patchProjectHourlyRate() {
            final BigDecimal hourlyRate = new BigDecimal("100");
            final Project patchedProject = createProject();
            final PatchProjectRequest patchProjectRequest = new PatchProjectRequest(
                    null,
                    JsonNullable.of(hourlyRate),
                    JsonNullable.undefined());
            final PatchProjectRequestDTO patchProjectRequestDTO = new PatchProjectRequestDTO(
                    null,
                    JsonNullable.of(hourlyRate),
                    JsonNullable.undefined());

            when(projectMapper.fromDTO(
                    eq(patchProjectRequestDTO),
                    eq(USER_ID)))
                    .thenReturn(patchProjectRequest);
            when(projectService.patchProject(
                    eq(USER_ID),
                    eq(PROJECT_ID),
                    eq(patchProjectRequest))).thenReturn(patchedProject);
            when(projectMapper.toDTO(eq(patchedProject))).thenReturn(createProjectDTO(hourlyRate, null));

            final String jsonBody = """
                    {
                        "hourlyRate": %s
                    }""".formatted(hourlyRate);

            assertThat(mockMvc.patch().uri(URL, PROJECT_ID).with(createPrincipal(USER_ID))
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(jsonBody))
                    .hasStatus(200)
                    .bodyJson()
                    .isEqualTo(createProjectDTOJson(hourlyRate, null));

            verify(projectMapper).fromDTO(
                    eq(patchProjectRequestDTO),
                    eq(USER_ID));
            verify(projectService).patchProject(
                    eq(USER_ID),
                    eq(PROJECT_ID),
                    eq(patchProjectRequest));
        }

        @Test
        void patchProjectClient() {
            final Project patchedProject = createProject();
            final PatchProjectRequest patchProjectRequest = new PatchProjectRequest(
                    null,
                    JsonNullable.undefined(),
                    JsonNullable.of(createClient()));
            final PatchProjectRequestDTO patchProjectRequestDTO = new PatchProjectRequestDTO(
                    null,
                    JsonNullable.undefined(),
                    JsonNullable.of(CLIENT_ID));

            when(projectMapper.fromDTO(
                    eq(patchProjectRequestDTO),
                    eq(USER_ID)))
                    .thenReturn(patchProjectRequest);
            when(projectService.patchProject(
                    eq(USER_ID),
                    eq(PROJECT_ID),
                    eq(patchProjectRequest))).thenReturn(patchedProject);
            when(projectMapper.toDTO(eq(patchedProject))).thenReturn(createProjectDTO(null, createClientDTO()));

            final String jsonBody = """
                    {
                        "clientId": %s
                    }""".formatted(CLIENT_ID);

            assertThat(mockMvc.patch().uri(URL, PROJECT_ID).with(createPrincipal(USER_ID))
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(jsonBody))
                    .hasStatus(200)
                    .bodyJson()
                    .isEqualTo(createProjectDTOJson(null, createClientDTOJson()));

            verify(projectMapper).fromDTO(
                    eq(patchProjectRequestDTO),
                    eq(USER_ID));
            verify(projectService).patchProject(
                    eq(USER_ID),
                    eq(PROJECT_ID),
                    eq(patchProjectRequest));
        }

        @Test
        void patchProjectWithoutChanges() {
            final Project patchedProject = createProject();
            final PatchProjectRequest patchProjectRequest = new PatchProjectRequest(
                    null,
                    JsonNullable.undefined(),
                    JsonNullable.undefined());
            final PatchProjectRequestDTO patchProjectRequestDTO = new PatchProjectRequestDTO(
                    null,
                    JsonNullable.undefined(),
                    JsonNullable.undefined());

            when(projectMapper.fromDTO(
                    eq(patchProjectRequestDTO),
                    eq(USER_ID)))
                    .thenReturn(patchProjectRequest);
            when(projectService.patchProject(
                    eq(USER_ID),
                    eq(PROJECT_ID),
                    eq(patchProjectRequest))).thenReturn(patchedProject);
            when(projectMapper.toDTO(eq(patchedProject))).thenReturn(createProjectDTO());

            assertThat(mockMvc.patch().uri(URL, PROJECT_ID).with(createPrincipal(USER_ID))
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("{}"))
                    .hasStatus(200)
                    .bodyJson()
                    .isEqualTo(createProjectDTOJson());

            verify(projectMapper).fromDTO(
                    eq(patchProjectRequestDTO),
                    eq(USER_ID));
            verify(projectService).patchProject(
                    eq(USER_ID),
                    eq(PROJECT_ID),
                    eq(patchProjectRequest));
        }

        @Test
        void patchProjectAllFields() {
            final BigDecimal hourlyRate = new BigDecimal("100");
            final Project patchedProject = createProject();
            final PatchProjectRequest patchProjectRequest = new PatchProjectRequest(
                    "MyProject",
                    JsonNullable.of(hourlyRate),
                    JsonNullable.of(createClient()));
            final PatchProjectRequestDTO patchProjectRequestDTO = new PatchProjectRequestDTO(
                    "MyProject",
                    JsonNullable.of(hourlyRate),
                    JsonNullable.of(CLIENT_ID));

            when(projectMapper.fromDTO(
                    eq(patchProjectRequestDTO),
                    eq(USER_ID)))
                    .thenReturn(patchProjectRequest);
            when(projectService.patchProject(
                    eq(USER_ID),
                    eq(PROJECT_ID),
                    eq(patchProjectRequest))).thenReturn(patchedProject);
            when(projectMapper.toDTO(eq(patchedProject))).thenReturn(createProjectDTO(hourlyRate, createClientDTO()));

            final String jsonBody = """
                    {
                        "name": "MyProject",
                        "hourlyRate": %s,
                        "clientId": %s
                    }""".formatted(hourlyRate, CLIENT_ID);

            assertThat(mockMvc.patch().uri(URL, PROJECT_ID).with(createPrincipal(USER_ID))
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(jsonBody))
                    .hasStatus(200)
                    .bodyJson()
                    .isEqualTo(createProjectDTOJson(hourlyRate, createClientDTOJson()));

            verify(projectMapper).fromDTO(
                    eq(patchProjectRequestDTO),
                    eq(USER_ID));
            verify(projectService).patchProject(
                    eq(USER_ID),
                    eq(PROJECT_ID),
                    eq(patchProjectRequest));
        }

        @Test
        void rejectsNonExistentClient() {
            final PatchProjectRequestDTO patchProjectRequestDTO = new PatchProjectRequestDTO(
                    null,
                    JsonNullable.undefined(),
                    JsonNullable.of(CLIENT_ID));
            final ClientNotFoundException exception = new ClientNotFoundException(CLIENT_ID);

            when(projectMapper.fromDTO(
                    eq(patchProjectRequestDTO),
                    eq(USER_ID)))
                    .thenThrow(exception);

            final String jsonBody = """
                    {
                        "clientId": %s
                    }""".formatted(CLIENT_ID);

            assertThat(mockMvc.patch().uri(URL, PROJECT_ID).with(createPrincipal(USER_ID))
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(jsonBody))
                    .hasStatus(404)
                    .bodyJson()
                    .extractingPath("$.error").asString()
                    .isEqualTo(exception.getMessage());

            verify(projectMapper).fromDTO(
                    eq(patchProjectRequestDTO),
                    eq(USER_ID));
            verifyNoInteractions(projectService);
        }

        @Test
        void rejectsUnauthenticatedRequest() {
            assertThat(mockMvc.patch().uri(URL, PROJECT_ID)).hasStatus(401);

            verifyNoInteractions(projectMapper, projectService);
        }

        @Test
        void rejectsNonExistentProject() {
            final PatchProjectRequest patchProjectRequest = createPatchProjectRequest();
            final PatchProjectRequestDTO patchProjectRequestDTO = createPatchProjectRequestDTO();
            final ProjectNotFoundException exception = new ProjectNotFoundException(PROJECT_ID);

            when(projectMapper.fromDTO(
                    eq(patchProjectRequestDTO),
                    eq(USER_ID)))
                    .thenReturn(patchProjectRequest);
            when(projectService.patchProject(
                    eq(USER_ID),
                    eq(PROJECT_ID),
                    eq(patchProjectRequest))).thenThrow(exception);

            assertThat(mockMvc.patch().uri(URL, PROJECT_ID).with(createPrincipal(USER_ID))
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(PATCH_PROJECT_JSON_BODY))
                    .hasStatus(404)
                    .bodyJson()
                    .extractingPath("$.error").asString()
                    .isEqualTo(exception.getMessage());

            verify(projectMapper).fromDTO(
                    eq(patchProjectRequestDTO),
                    eq(USER_ID));
            verify(projectService).patchProject(
                    eq(USER_ID),
                    eq(PROJECT_ID),
                    eq(patchProjectRequest));
        }

        @Test
        void rejectsEmptyName() {
            final String jsonBody = """
                    {
                        "name": ""
                    }""";

            assertThat(mockMvc.patch().uri(URL, PROJECT_ID).with(createPrincipal(USER_ID))
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(jsonBody))
                    .hasStatus(400)
                    .bodyJson()
                    .extractingPath("$.error").asString().isEqualTo(PatchProjectRequestDTO.ERROR_MESSAGE_EMPTY_NAME);

            verifyNoInteractions(projectMapper, projectService);
        }

        @Test
        void rejectsBlankName() {
            final String jsonBody = """
                    {
                        "name": "  "
                    }""";

            assertThat(mockMvc.patch().uri(URL, PROJECT_ID).with(createPrincipal(USER_ID))
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(jsonBody))
                    .hasStatus(400)
                    .bodyJson()
                    .extractingPath("$.error").asString().isEqualTo(PatchProjectRequestDTO.ERROR_MESSAGE_EMPTY_NAME);

            verifyNoInteractions(projectMapper, projectService);
        }

        @Test
        void rejectsNegativeHourlyRate() {
            final String jsonBody = """
                    {
                        "hourlyRate": -1
                    }""";

            assertThat(mockMvc.patch().uri(URL, PROJECT_ID).with(createPrincipal(USER_ID))
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(jsonBody))
                    .hasStatus(400)
                    .bodyJson()
                    .extractingPath("$.error").asString().isEqualTo(PatchProjectRequestDTO.ERROR_MESSAGE_NEGATIVE_RATE);

            verifyNoInteractions(projectMapper, projectService);
        }
    }

    @Nested
    class DeleteProject {
        private static final String URL = "/api/v1/projects/{projectId}";

        @Test
        void deletesProject() {
            assertThat(mockMvc.delete().uri(URL, PROJECT_ID).with(createPrincipal(USER_ID)))
                    .hasStatus(204);

            verify(projectService).deleteProject(USER_ID, PROJECT_ID);
        }

        @Test
        void rejectsUnauthenticatedRequest() {
            assertThat(mockMvc.delete().uri(URL, PROJECT_ID)).hasStatus(401);

            verifyNoInteractions(projectService);
        }

        @Test
        void rejectsNonExistentProject() {
            final ProjectNotFoundException exception = new ProjectNotFoundException(PROJECT_ID);
            doThrow(exception).when(projectService).deleteProject(USER_ID, PROJECT_ID);

            assertThat(mockMvc.delete().uri(URL, PROJECT_ID).with(createPrincipal(USER_ID)))
                    .hasStatus(404)
                    .bodyJson()
                    .extractingPath("$.error").asString()
                    .isEqualTo(exception.getMessage());

            verify(projectService).deleteProject(USER_ID, PROJECT_ID);
        }
    }

    private static CreateProjectRequest createProjectRequest() {
        return new CreateProjectRequest("MyProject", createUser(USER_ID), null, null);
    }

    private static CreateProjectRequestDTO createCreateProjectRequestDTO() {
        return new CreateProjectRequestDTO("MyProject", null, null);
    }

    private static PatchProjectRequest createPatchProjectRequest() {
        return new PatchProjectRequest(
                "MyProject",
                JsonNullable.undefined(),
                JsonNullable.undefined());
    }

    private static PatchProjectRequestDTO createPatchProjectRequestDTO() {
        return new PatchProjectRequestDTO(
                "MyProject",
                JsonNullable.undefined(),
                JsonNullable.undefined());
    }
}
