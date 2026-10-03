package me.eeshe.tempus.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openapitools.jackson.nullable.JsonNullable;

import me.eeshe.tempus.entity.Client;
import me.eeshe.tempus.entity.Project;
import me.eeshe.tempus.entity.User;
import me.eeshe.tempus.exception.ProjectNotFoundException;
import me.eeshe.tempus.exception.UserProjectAlreadyExistsException;
import me.eeshe.tempus.repository.ProjectRepository;
import me.eeshe.tempus.request.CreateProjectRequest;
import me.eeshe.tempus.request.PatchProjectRequest;
import me.eeshe.tempus.support.EntityTestBase;

@ExtendWith(MockitoExtension.class)
public class ProjectServiceImplTest extends EntityTestBase {
    private static final BigDecimal HOURLY_RATE = new BigDecimal("50.00");

    @Mock
    private ProjectRepository projectRepository;

    @InjectMocks
    private ProjectServiceImpl projectService;

    @Nested
    class ListProjects {

        @Test
        void returnsAllUserProjects() {
            final Project firstProject = createProject();
            final Project secondProject = createSecondProject();

            when(projectRepository.findByUserId(USER_ID)).thenReturn(List.of(firstProject, secondProject));

            final List<Project> projects = projectService.listProjects(USER_ID);

            assertThat(projects).containsExactly(firstProject, secondProject);
            verify(projectRepository).findByUserId(USER_ID);
        }
    }

    @Nested
    class GetProject {

        @Test
        void returnsProjectWhenFound() {
            final Project project = createProject();

            when(projectRepository.findByIdAndUserId(PROJECT_ID, USER_ID)).thenReturn(Optional.of(project));

            assertThat(projectService.getProject(USER_ID, PROJECT_ID)).isEqualTo(project);
        }

        @Test
        void throwsWhenProjectNotFound() {
            when(projectRepository.findByIdAndUserId(PROJECT_ID, USER_ID)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> projectService.getProject(USER_ID, PROJECT_ID))
                    .isInstanceOf(ProjectNotFoundException.class)
                    .hasMessage("Project with ID 20 does not exist");
        }
    }

    @Nested
    class GetProjectByName {

        @Test
        void returnsProjectWhenNameMatches() {
            final Project project = createProject();

            when(projectRepository.findByUserIdAndName(USER_ID, "MyProject")).thenReturn(Optional.of(project));

            assertThat(projectService.getProject(USER_ID, "MyProject")).contains(project);
        }

        @Test
        void returnsEmptyWhenProjectDoesNotExist() {
            when(projectRepository.findByUserIdAndName(USER_ID, "MyProject")).thenReturn(Optional.empty());

            assertThat(projectService.getProject(USER_ID, "MyProject")).isEmpty();
        }
    }

    @Nested
    class CreateProject {

        @Test
        void savesProjectWithRequestFields() {
            final User user = createUser(USER_ID);
            final Client client = createClient();
            final CreateProjectRequest createProjectRequest = new CreateProjectRequest(
                    "MyProject", user, HOURLY_RATE, client);
            final Project savedProject = createProject();

            when(projectRepository.findByUserIdAndName(USER_ID, "MyProject")).thenReturn(Optional.empty());
            when(projectRepository.save(any(Project.class))).thenReturn(savedProject);

            final Project project = projectService.createProject(createProjectRequest);

            final ArgumentCaptor<Project> projectCaptor = ArgumentCaptor.forClass(Project.class);

            verify(projectRepository).save(projectCaptor.capture());

            assertThat(projectCaptor.getValue().getName()).isEqualTo("MyProject");
            assertThat(projectCaptor.getValue().getUser()).isEqualTo(user);
            assertThat(projectCaptor.getValue().getHourlyRate()).isEqualByComparingTo(HOURLY_RATE);
            assertThat(projectCaptor.getValue().getClient()).isEqualTo(client);
            assertThat(project).isEqualTo(savedProject);
        }

        @Test
        void throwsWhenUserProjectAlreadyExists() {
            final User user = createUser(USER_ID);
            final Client client = createClient();
            final Project existingProject = createProject();

            when(projectRepository.findByUserIdAndName(USER_ID, "MyProject"))
                    .thenReturn(Optional.of(existingProject));

            assertThatThrownBy(() -> projectService.createProject(
                    new CreateProjectRequest("MyProject", user, HOURLY_RATE, client)))
                    .isInstanceOf(UserProjectAlreadyExistsException.class)
                    .hasMessage("User 1 already has a project named MyProject");

            verify(projectRepository, never()).save(any());
        }
    }

    @Nested
    class PatchProject {

        @Test
        void patchesNameAndSaves() {
            final Project project = createProject();

            when(projectRepository.findByIdAndUserId(PROJECT_ID, USER_ID)).thenReturn(Optional.of(project));
            when(projectRepository.save(project)).thenReturn(project);

            final Project patchedProject = projectService.patchProject(USER_ID, PROJECT_ID, new PatchProjectRequest(
                    "MyNewProject", JsonNullable.undefined(), JsonNullable.undefined()));

            assertThat(patchedProject.getName()).isEqualTo("MyNewProject");

            verify(projectRepository).save(project);
        }

        @Test
        void patchesHourlyRateAndSaves() {
            final Project project = createProject();

            when(projectRepository.findByIdAndUserId(PROJECT_ID, USER_ID)).thenReturn(Optional.of(project));
            when(projectRepository.save(project)).thenReturn(project);

            final Project patchedProject = projectService.patchProject(USER_ID, PROJECT_ID, new PatchProjectRequest(
                    null, JsonNullable.of(HOURLY_RATE), JsonNullable.undefined()));

            assertThat(patchedProject.getHourlyRate()).isEqualByComparingTo(HOURLY_RATE);
            assertThat(patchedProject.getName()).isEqualTo("MyProject");

            verify(projectRepository).save(project);
        }

        @Test
        void patchesClientAndSaves() {
            final Client client = createClient();
            final Project project = createProject();

            when(projectRepository.findByIdAndUserId(PROJECT_ID, USER_ID)).thenReturn(Optional.of(project));
            when(projectRepository.save(project)).thenReturn(project);

            final Project patchedProject = projectService.patchProject(USER_ID, PROJECT_ID, new PatchProjectRequest(
                    null, JsonNullable.undefined(), JsonNullable.of(client)));

            assertThat(patchedProject.getClient()).isEqualTo(client);
            assertThat(patchedProject.getName()).isEqualTo("MyProject");

            verify(projectRepository).save(project);
        }

        @Test
        void patchesNothingWhenAllFieldsNull() {
            final Project project = createProject();

            when(projectRepository.findByIdAndUserId(PROJECT_ID, USER_ID)).thenReturn(Optional.of(project));
            when(projectRepository.save(project)).thenReturn(project);

            final Project patchedProject = projectService.patchProject(USER_ID, PROJECT_ID, new PatchProjectRequest(
                    null, JsonNullable.undefined(), JsonNullable.undefined()));

            assertThat(patchedProject.getName()).isEqualTo("MyProject");
            assertThat(patchedProject.getHourlyRate()).isNull();
            assertThat(patchedProject.getClient()).isNull();

            verify(projectRepository).save(project);
        }

        @Test
        void throwsWhenProjectNotFound() {
            when(projectRepository.findByIdAndUserId(PROJECT_ID, USER_ID)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> projectService.patchProject(USER_ID, PROJECT_ID, new PatchProjectRequest(
                    "MyNewProject", JsonNullable.undefined(), JsonNullable.undefined())))
                    .isInstanceOf(ProjectNotFoundException.class)
                    .hasMessage("Project with ID 20 does not exist");

            verify(projectRepository, never()).save(any());
        }
    }

    @Nested
    class DeleteProject {

        @Test
        void deletesProjectWhenFound() {
            final Project project = createProject();

            when(projectRepository.findByIdAndUserId(PROJECT_ID, USER_ID)).thenReturn(Optional.of(project));

            projectService.deleteProject(USER_ID, PROJECT_ID);

            verify(projectRepository).deleteById(PROJECT_ID);
        }

        @Test
        void throwsWhenProjectNotFound() {
            when(projectRepository.findByIdAndUserId(PROJECT_ID, USER_ID)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> projectService.deleteProject(USER_ID, PROJECT_ID))
                    .isInstanceOf(ProjectNotFoundException.class)
                    .hasMessage("Project with ID 20 does not exist");

            verify(projectRepository, never()).deleteById(anyLong());
        }
    }
}
