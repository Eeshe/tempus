package me.eeshe.tempus.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import me.eeshe.tempus.entity.Project;
import me.eeshe.tempus.entity.Task;
import me.eeshe.tempus.entity.TimeEntry;
import me.eeshe.tempus.entity.User;
import me.eeshe.tempus.support.RepositoryTestBase;

public class TaskRepositoryTest extends RepositoryTestBase {
    private static final Instant START_TIME = Instant.parse("2026-01-01T10:00:00Z");
    private static final Instant END_TIME = Instant.parse("2026-01-01T11:00:00Z");

    @Nested
    class FindByUserId {

        @Test
        void returnsUserTasks() {
            final User user = createUser("MyUser");
            final User otherUser = createUser("MyOtherUser");

            final Project project = createProject("MyProject", user);

            final Task firstTask = createTask("MyTask", user, project);
            final Task secondTask = createTask("MySecondTask", user, project);

            createTask("MyOtherTask", otherUser, createProject("MyOtherProject", otherUser));

            final List<Task> tasks = taskRepository.findByUserId(user.getId());

            assertThat(tasks).containsExactlyInAnyOrder(firstTask, secondTask);
        }

        @Test
        void returnsEmptyWhenUserHasNoTasks() {
            final User user = createUser("MyUser");

            assertThat(taskRepository.findByUserId(user.getId())).isEmpty();
        }
    }

    @Nested
    class FindByIdAndUserId {

        @Test
        void returnsTaskWhenOwnedByUser() {
            final User user = createUser("MyUser");
            final Project project = createProject("MyProject", user);
            final Task task = createTask("MyTask", user, project);

            final Optional<Task> found = taskRepository.findByIdAndUserId(task.getId(), user.getId());

            assertThat(found).contains(task);
        }

        @Test
        void returnsEmptyWhenOwnedByOtherUser() {
            final User user = createUser("MyUser");
            final User otherUser = createUser("MyOtherUser");

            final Project project = createProject("MyProject", user);
            final Task task = createTask("MyTask", user, project);

            assertThat(taskRepository.findByIdAndUserId(task.getId(), otherUser.getId())).isEmpty();
        }
    }

    @Nested
    class FindByUserIdAndNameAndProjectId {

        @Test
        void returnsTaskWhenNameAndProjectMatch() {
            final User user = createUser("MyUser");
            final Project project = createProject("MyProject", user);
            final Task task = createTask("MyTask", user, project);

            final Optional<Task> found = taskRepository
                    .findByUserIdAndNameAndProjectId(user.getId(), "MyTask", project.getId());

            assertThat(found).contains(task);
        }

        @Test
        void returnsTaskWhenOtherProjectHasSameTaskName() {
            final User user = createUser("MyUser");

            final Project project = createProject("MyProject", user);
            final Project otherProject = createProject("MyOtherProject", user);

            final Task task = createTask("MyTask", user, project);

            createTask("MyTask", user, otherProject);

            final Optional<Task> found = taskRepository
                    .findByUserIdAndNameAndProjectId(user.getId(), "MyTask", project.getId());

            assertThat(found).contains(task);
        }

        @Test
        void returnsEmptyWhenNameDoesNotMatch() {
            final User user = createUser("MyUser");
            final Project project = createProject("MyProject", user);

            createTask("MyTask", user, project);

            assertThat(taskRepository.findByUserIdAndNameAndProjectId(user.getId(), "MyOtherTask", project.getId()))
                    .isEmpty();
        }

        @Test
        void returnsEmptyWhenProjectDoesNotMatch() {
            final User user = createUser("MyUser");

            final Project project = createProject("MyProject", user);
            final Project otherProject = createProject("MyOtherProject", user);

            createTask("MyTask", user, project);

            assertThat(taskRepository
                    .findByUserIdAndNameAndProjectId(user.getId(), "MyTask", otherProject.getId()))
                    .isEmpty();
        }

        @Test
        void returnsEmptyWhenOwnedByOtherUser() {
            final User user = createUser("MyUser");
            final User otherUser = createUser("MyOtherUser");

            final Project project = createProject("MyProject", user);

            createTask("MyTask", user, project);

            assertThat(taskRepository
                    .findByUserIdAndNameAndProjectId(otherUser.getId(), "MyTask", project.getId()))
                    .isEmpty();
        }
    }

    @Nested
    class SaveTask {

        @Test
        void savePersistsTaskWithProjectAndSetsGeneratedFields() {
            final User user = createUser("MyUser");
            final Project project = createProject("MyProject", user);
            final Task saved = taskRepository.save(new Task("MyTask", user, project));

            assertThat(saved.getId()).isPositive();
            assertThat(saved.getCreatedAt()).isNotNull();

            entityManager.flush();
            entityManager.clear();

            final Optional<Task> found = taskRepository.findById(saved.getId());

            assertThat(found).isPresent();
            assertThat(found.get().getProject()).isEqualTo(project);
        }
    }

    @Nested
    class DeleteTask {

        @Test
        void deleteRemovesTask() {
            final User user = createUser("MyUser");
            final Project project = createProject("MyProject", user);
            final Task task = createTask("MyTask", user, project);

            taskRepository.deleteById(task.getId());

            assertThat(taskRepository.findByIdAndUserId(task.getId(), user.getId())).isEmpty();
        }

        @Test
        void deleteSetsCorrespondingTimeEntriesTasksToNull() {
            final User user = createUser("MyUser");
            final Project project = createProject("MyProject", user);
            final Task task = createTask("MyTask", user, project);
            final TimeEntry timeEntry = createTimeEntry(
                    user, project, task, "MyDescription", true, START_TIME, END_TIME);

            entityManager.flush();
            entityManager.clear();

            taskRepository.deleteById(task.getId());
            entityManager.flush();
            entityManager.clear();

            final Optional<TimeEntry> found = timeEntryRepository.findById(timeEntry.getId());

            assertThat(found).isPresent();
            assertThat(found.get().getTask()).isNull();
        }

        @Test
        void deleteLeavesUserIntact() {
            final User user = createUser("MyUser");
            final Project project = createProject("MyProject", user);
            final Task task = createTask("MyTask", user, project);

            taskRepository.deleteById(task.getId());
            entityManager.flush();
            entityManager.clear();

            assertThat(userRepository.findById(user.getId())).isPresent();
        }

        @Test
        void deleteLeavesProjectIntact() {
            final User user = createUser("MyUser");
            final Project project = createProject("MyProject", user);
            final Task task = createTask("MyTask", user, project);

            taskRepository.deleteById(task.getId());
            entityManager.flush();
            entityManager.clear();

            assertThat(projectRepository.findById(project.getId())).isPresent();
        }
    }
}
