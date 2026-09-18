package me.eeshe.tempus.service.impl;

import java.util.List;

import org.springframework.stereotype.Service;

import me.eeshe.tempus.entity.Task;
import me.eeshe.tempus.exception.TaskNotFoundException;
import me.eeshe.tempus.exception.UserProjectTaskAlreadyExistsException;
import me.eeshe.tempus.repository.TaskRepository;
import me.eeshe.tempus.request.CreateTaskRequest;
import me.eeshe.tempus.request.PatchTaskRequest;
import me.eeshe.tempus.service.TaskService;

@Service
public class TaskServiceImpl implements TaskService {
    private final TaskRepository taskRepository;

    public TaskServiceImpl(TaskRepository taskRepository) {
        this.taskRepository = taskRepository;
    }

    @Override
    public List<Task> listTasks(long userId) {
        return taskRepository.findByUserId(userId);
    }

    @Override
    public Task getTask(long userId, long taskId) {
        return taskRepository.findByIdAndUserId(taskId, userId)
                .orElseThrow(() -> new TaskNotFoundException(taskId));
    }

    @Override
    public Task createTask(CreateTaskRequest createTaskRequest) {
        final long userId = createTaskRequest.user().getId();
        final String taskName = createTaskRequest.name();
        final long projectId = createTaskRequest.project().getId();
        taskRepository.findByUserIdAndNameAndProjectId(userId, taskName, projectId).ifPresent(task -> {
            throw new UserProjectTaskAlreadyExistsException(userId, taskName, task.getProject().getName());
        });
        return taskRepository.save(new Task(
                createTaskRequest.name(),
                createTaskRequest.user(),
                createTaskRequest.project()));
    }

    @Override
    public Task patchTask(long userId, long taskId, PatchTaskRequest patchTaskRequest) {
        final Task task = getTask(userId, taskId);
        if (patchTaskRequest.name() != null) {
            task.setName(patchTaskRequest.name());
        }
        return taskRepository.save(task);
    }

    @Override
    public void deleteTask(long userId, long taskId) {
        getTask(userId, taskId);
        taskRepository.deleteById(taskId);
    }
}
