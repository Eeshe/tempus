package me.eeshe.tempus.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Instant;
import java.util.Optional;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.orm.jpa.JpaSystemException;

import me.eeshe.tempus.entity.Client;
import me.eeshe.tempus.entity.Project;
import me.eeshe.tempus.entity.Task;
import me.eeshe.tempus.entity.TimeEntry;
import me.eeshe.tempus.entity.User;
import me.eeshe.tempus.support.RepositoryTestBase;

public class UserRepositoryTest extends RepositoryTestBase {
    private static final Instant START_TIME = Instant.parse("2026-01-01T10:00:00Z");
    private static final Instant END_TIME = Instant.parse("2026-01-01T11:00:00Z");

    @Nested
    class FindByName {

        @Test
        void returnsUserWhenNameMatches() {
            final User user = createUser("MyUser");

            assertThat(userRepository.findByName("MyUser")).contains(user);
        }

        @Test
        void returnsEmptyWhenNameDoesNotMatch() {
            createUser("MyUser");

            assertThat(userRepository.findByName("MyOtherUser")).isEmpty();
        }

        @Test
        void returnsEmptyWhenNameDiffersOnlyByCase() {
            createUser("MyUser");

            assertThat(userRepository.findByName("myuser")).isEmpty();
        }
    }

    @Nested
    class SaveUser {

        @Test
        void savePersistsUserAndSetsGeneratedFields() {
            final User saved = userRepository.save(new User("MyUser", "MyPassword"));

            assertThat(saved.getId()).isPositive();
            assertThat(saved.getCreatedAt()).isNotNull();

            entityManager.flush();
            entityManager.clear();

            final Optional<User> found = userRepository.findById(saved.getId());

            assertThat(found).isPresent();
            assertThat(found.get().getName()).isEqualTo("MyUser");
            assertThat(found.get().getPassword()).isEqualTo("MyPassword");
        }

        @Test
        void saveFailsWhenNameAlreadyExists() {
            createUser("MyUser");

            assertThatThrownBy(() -> {
                userRepository.save(new User("MyUser", "MyPassword"));
                entityManager.flush();
            }).isInstanceOf(JpaSystemException.class)
                    .hasMessageContaining("UNIQUE constraint failed: users.name");
        }
    }

    @Nested
    class DeleteUser {

        @Test
        void deleteRemovesUser() {
            final User user = createUser("MyUser");

            userRepository.deleteById(user.getId());

            assertThat(userRepository.findById(user.getId())).isEmpty();
        }

        @Test
        void deleteCascadesToClients() {
            final User user = createUser("MyUser");
            final Client client = createClient("MyClient", user);

            entityManager.flush();
            entityManager.clear();

            userRepository.deleteById(user.getId());
            entityManager.flush();
            entityManager.clear();

            assertThat(clientRepository.findById(client.getId())).isEmpty();
        }

        @Test
        void deleteCascadesToProjects() {
            final User user = createUser("MyUser");
            final Project project = createProject("MyProject", user);

            entityManager.flush();
            entityManager.clear();

            userRepository.deleteById(user.getId());
            entityManager.flush();
            entityManager.clear();

            assertThat(projectRepository.findById(project.getId())).isEmpty();
        }

        @Test
        void deleteCascadesToTasks() {
            final User user = createUser("MyUser");
            final Project project = createProject("MyProject", user);
            final Task task = createTask("MyTask", user, project);

            entityManager.flush();
            entityManager.clear();

            userRepository.deleteById(user.getId());
            entityManager.flush();
            entityManager.clear();

            assertThat(taskRepository.findById(task.getId())).isEmpty();
        }

        @Test
        void deleteCascadesToTimeEntries() {
            final User user = createUser("MyUser");
            final Project project = createProject("MyProject", user);
            final TimeEntry timeEntry = createTimeEntry(
                    user, project, null, "MyDescription", true, START_TIME, END_TIME);

            entityManager.flush();
            entityManager.clear();

            userRepository.deleteById(user.getId());
            entityManager.flush();
            entityManager.clear();

            assertThat(timeEntryRepository.findById(timeEntry.getId())).isEmpty();
        }

        @Test
        void deleteLeavesOtherUsersDataUntouched() {
            final User user = createUser("MyUser");
            final User otherUser = createUser("MyOtherUser");
            final Client otherClient = createClient("MyClient", otherUser);
            final Project otherProject = createProject("MyProject", otherUser);
            final Task otherTask = createTask("MyTask", otherUser, otherProject);
            final TimeEntry otherTimeEntry = createTimeEntry(
                    otherUser, otherProject, otherTask, "MyDescription", true, START_TIME, END_TIME);

            entityManager.flush();
            entityManager.clear();

            userRepository.deleteById(user.getId());
            entityManager.flush();
            entityManager.clear();

            assertThat(userRepository.findById(otherUser.getId())).isPresent();
            assertThat(clientRepository.findById(otherClient.getId())).isPresent();
            assertThat(projectRepository.findById(otherProject.getId())).isPresent();
            assertThat(taskRepository.findById(otherTask.getId())).isPresent();
            assertThat(timeEntryRepository.findById(otherTimeEntry.getId())).isPresent();
        }
    }
}
