package me.eeshe.tempus.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import me.eeshe.tempus.entity.Client;
import me.eeshe.tempus.entity.Project;
import me.eeshe.tempus.entity.Task;
import me.eeshe.tempus.entity.TimeEntry;
import me.eeshe.tempus.entity.User;
import me.eeshe.tempus.support.RepositoryTestBase;

public class ProjectRepositoryTest extends RepositoryTestBase {
    private static final BigDecimal HOURLY_RATE = new BigDecimal("50.00");

    private static final Instant START_TIME = Instant.parse("2026-01-01T10:00:00Z");
    private static final Instant END_TIME = Instant.parse("2026-01-01T11:00:00Z");

    @Nested
    class FindByUserId {

        @Test
        void returnsUserProjects() {
            final User user = createUser("MyUser");
            final User otherUser = createUser("MyOtherUser");

            final Project firstProject = createProject("MyProject", user);
            final Project secondProject = createProject("MySecondProject", user);

            createProject("MyOtherProject", otherUser);

            final List<Project> projects = projectRepository.findByUserId(user.getId());

            assertThat(projects).containsExactlyInAnyOrder(firstProject, secondProject);
        }

        @Test
        void returnsEmptyWhenUserHasNoProjects() {
            final User user = createUser("MyUser");

            assertThat(projectRepository.findByUserId(user.getId())).isEmpty();
        }
    }

    @Nested
    class FindByIdAndUserId {

        @Test
        void returnsProjectWhenOwnedByUser() {
            final User user = createUser("MyUser");
            final Project project = createProject("MyProject", user);

            final Optional<Project> found = projectRepository.findByIdAndUserId(project.getId(), user.getId());

            assertThat(found).contains(project);
        }

        @Test
        void returnsEmptyWhenOwnedByOtherUser() {
            final User user = createUser("MyUser");
            final User otherUser = createUser("MyOtherUser");

            final Project project = createProject("MyProject", user);

            assertThat(projectRepository.findByIdAndUserId(project.getId(), otherUser.getId())).isEmpty();
        }
    }

    @Nested
    class FindByUserIdAndName {

        @Test
        void returnsProjectWhenNameMatches() {
            final User user = createUser("MyUser");
            final Project project = createProject("MyProject", user);

            final Optional<Project> found = projectRepository.findByUserIdAndName(user.getId(), "MyProject");

            assertThat(found).contains(project);
        }

        @Test
        void returnsUserProjectWhenOtherUserHasSameProjectName() {
            final User user = createUser("MyUser");
            final User otherUser = createUser("MyOtherUser");
            final Project project = createProject("MyProject", user);

            createProject("MyProject", otherUser);

            final Optional<Project> found = projectRepository.findByUserIdAndName(user.getId(), "MyProject");

            assertThat(found).contains(project);
        }

        @Test
        void returnsEmptyWhenNameDoesNotMatch() {
            final User user = createUser("MyUser");
            createProject("MyProject", user);

            assertThat(projectRepository.findByUserIdAndName(user.getId(), "MyOtherProject")).isEmpty();
        }
    }

    @Nested
    class SaveProject {

        @Test
        void savePersistsProjectWithClientAndSetsGeneratedFields() {
            final User user = createUser("MyUser");
            final Client client = createClient("MyClient", user);

            final Project saved = projectRepository
                    .save(new Project("MyProject", user, HOURLY_RATE, client));

            assertThat(saved.getId()).isPositive();
            assertThat(saved.getCreatedAt()).isNotNull();

            entityManager.flush();
            entityManager.clear();

            final Optional<Project> found = projectRepository.findById(saved.getId());

            assertThat(found).isPresent();
            assertThat(found.get().getHourlyRate()).isEqualByComparingTo(HOURLY_RATE);
            assertThat(found.get().getClient()).isEqualTo(client);
        }
    }

    @Nested
    class DeleteProject {

        @Test
        void deleteRemovesProject() {
            final User user = createUser("MyUser");
            final Project project = createProject("MyProject", user);

            projectRepository.deleteById(project.getId());

            assertThat(projectRepository.findByIdAndUserId(project.getId(), user.getId())).isEmpty();
        }

        @Test
        void deleteCascadesToTasks() {
            final User user = createUser("MyUser");
            final Project project = createProject("MyProject", user);
            final Task task = createTask("MyTask", user, project);

            entityManager.flush();
            entityManager.clear();

            projectRepository.deleteById(project.getId());
            entityManager.flush();
            entityManager.clear();

            assertThat(taskRepository.findById(task.getId())).isEmpty();
        }

        @Test
        void deleteCascadesToTimeEntries() {
            final User user = createUser("MyUser");
            final Project project = createProject("MyProject", user);
            final TimeEntry timeEntry = createTimeEntry(
                    user,
                    project,
                    null,
                    "MyDescription",
                    true,
                    START_TIME,
                    END_TIME);

            entityManager.flush();
            entityManager.clear();

            projectRepository.deleteById(project.getId());
            entityManager.flush();
            entityManager.clear();

            assertThat(timeEntryRepository.findById(timeEntry.getId())).isEmpty();
        }

        @Test
        void deleteLeavesUserIntact() {
            final User user = createUser("MyUser");
            final Project project = createProject("MyProject", user);

            projectRepository.deleteById(project.getId());
            entityManager.flush();
            entityManager.clear();

            assertThat(userRepository.findById(user.getId())).isPresent();
        }

        @Test
        void deleteLeavesClientIntact() {
            final User user = createUser("MyUser");
            final Client client = createClient("MyClient", user);
            final Project project = createProject("MyProject", user, null, client);

            projectRepository.deleteById(project.getId());
            entityManager.flush();
            entityManager.clear();

            assertThat(clientRepository.findByIdAndUserId(client.getId(), user.getId())).contains(client);
        }
    }
}
