package me.eeshe.tempus.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import me.eeshe.tempus.entity.Project;
import me.eeshe.tempus.entity.Task;
import me.eeshe.tempus.entity.User;
import me.eeshe.tempus.exception.TaskNotFoundException;
import me.eeshe.tempus.exception.UserProjectTaskAlreadyExistsException;
import me.eeshe.tempus.repository.TaskRepository;
import me.eeshe.tempus.request.CreateTaskRequest;
import me.eeshe.tempus.request.PatchTaskRequest;
import me.eeshe.tempus.support.EntityTestBase;

@ExtendWith(MockitoExtension.class)
public class TaskServiceImplTest extends EntityTestBase {

    @Mock
    private TaskRepository taskRepository;

    @InjectMocks
    private TaskServiceImpl taskService;

    @Nested
    class ListTasks {

        @Test
        void returnsAllUserTasks() {
            final Task firstTask = createTask();
            final Task secondTask = createSecondTask();

            when(taskRepository.findByUserId(USER_ID)).thenReturn(List.of(firstTask, secondTask));

            final List<Task> tasks = taskService.listTasks(USER_ID);

            assertThat(tasks).containsExactly(firstTask, secondTask);
            verify(taskRepository).findByUserId(USER_ID);
        }
    }

    @Nested
    class GetTask {

        @Test
        void returnsTaskWhenFound() {
            final Task task = createTask();

            when(taskRepository.findByIdAndUserId(TASK_ID, USER_ID)).thenReturn(Optional.of(task));

            assertThat(taskService.getTask(USER_ID, TASK_ID)).isEqualTo(task);
        }

        @Test
        void throwsWhenTaskNotFound() {
            when(taskRepository.findByIdAndUserId(TASK_ID, USER_ID)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> taskService.getTask(USER_ID, TASK_ID))
                    .isInstanceOf(TaskNotFoundException.class)
                    .hasMessage("Task with ID 300 does not exist");
        }
    }

    @Nested
    class GetTaskByName {

        @Test
        void returnsTaskWhenNameAndProjectMatch() {
            final Task task = createTask();

            when(taskRepository.findByUserIdAndNameAndProjectId(USER_ID, "MyTask", PROJECT_ID))
                    .thenReturn(Optional.of(task));

            assertThat(taskService.getTask(USER_ID, "MyTask", PROJECT_ID)).contains(task);
        }

        @Test
        void returnsEmptyWhenTaskDoesNotExist() {
            when(taskRepository.findByUserIdAndNameAndProjectId(USER_ID, "MyTask", PROJECT_ID))
                    .thenReturn(Optional.empty());

            assertThat(taskService.getTask(USER_ID, "MyTask", PROJECT_ID)).isEmpty();
        }
    }

    @Nested
    class CreateTask {

        @Test
        void savesTaskWithRequestFields() {
            final User user = createUser(USER_ID);
            final Project project = createProject();
            final CreateTaskRequest createTaskRequest = new CreateTaskRequest("MyTask", user, project);
            final Task savedTask = createTask();

            when(taskRepository.findByUserIdAndNameAndProjectId(USER_ID, "MyTask", PROJECT_ID))
                    .thenReturn(Optional.empty());
            when(taskRepository.save(any(Task.class))).thenReturn(savedTask);

            final Task task = taskService.createTask(createTaskRequest);

            final ArgumentCaptor<Task> taskCaptor = ArgumentCaptor.forClass(Task.class);

            verify(taskRepository).save(taskCaptor.capture());

            assertThat(taskCaptor.getValue().getName()).isEqualTo("MyTask");
            assertThat(taskCaptor.getValue().getUser()).isEqualTo(user);
            assertThat(taskCaptor.getValue().getProject()).isEqualTo(project);
            assertThat(task).isEqualTo(savedTask);
        }

        @Test
        void throwsWhenUserProjectTaskAlreadyExists() {
            final User user = createUser(USER_ID);
            final Project project = createProject();
            final Task existingTask = createTask();

            when(taskRepository.findByUserIdAndNameAndProjectId(USER_ID, "MyTask", PROJECT_ID))
                    .thenReturn(Optional.of(existingTask));

            assertThatThrownBy(() -> taskService.createTask(new CreateTaskRequest("MyTask", user, project)))
                    .isInstanceOf(UserProjectTaskAlreadyExistsException.class)
                    .hasMessage("User 1 already has a task named MyTask for project MyProject");

            verify(taskRepository, never()).save(any());
        }
    }

    @Nested
    class PatchTask {

        @Test
        void patchesNameAndSaves() {
            final Task task = createTask();

            when(taskRepository.findByIdAndUserId(TASK_ID, USER_ID)).thenReturn(Optional.of(task));
            when(taskRepository.save(task)).thenReturn(task);

            final Task patchedTask = taskService.patchTask(USER_ID, TASK_ID, new PatchTaskRequest("MyNewTask"));

            assertThat(patchedTask.getName()).isEqualTo("MyNewTask");

            verify(taskRepository).save(task);
        }

        @Test
        void patchesNothingWhenNameNull() {
            final Task task = createTask();

            when(taskRepository.findByIdAndUserId(TASK_ID, USER_ID)).thenReturn(Optional.of(task));
            when(taskRepository.save(task)).thenReturn(task);

            final Task patchedTask = taskService.patchTask(USER_ID, TASK_ID, new PatchTaskRequest(null));

            assertThat(patchedTask.getName()).isEqualTo("MyTask");

            verify(taskRepository).save(task);
        }

        @Test
        void throwsWhenTaskNotFound() {
            when(taskRepository.findByIdAndUserId(TASK_ID, USER_ID)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> taskService.patchTask(USER_ID, TASK_ID, new PatchTaskRequest("MyNewTask")))
                    .isInstanceOf(TaskNotFoundException.class)
                    .hasMessage("Task with ID 300 does not exist");

            verify(taskRepository, never()).save(any());
        }
    }

    @Nested
    class DeleteTask {

        @Test
        void deletesTaskWhenFound() {
            final Task task = createTask();

            when(taskRepository.findByIdAndUserId(TASK_ID, USER_ID)).thenReturn(Optional.of(task));

            taskService.deleteTask(USER_ID, TASK_ID);

            verify(taskRepository).deleteById(TASK_ID);
        }

        @Test
        void throwsWhenTaskNotFound() {
            when(taskRepository.findByIdAndUserId(TASK_ID, USER_ID)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> taskService.deleteTask(USER_ID, TASK_ID))
                    .isInstanceOf(TaskNotFoundException.class)
                    .hasMessage("Task with ID 300 does not exist");

            verify(taskRepository, never()).deleteById(anyLong());
        }
    }
}
