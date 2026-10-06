package me.eeshe.tempus.service;

import java.util.List;
import java.util.Optional;

import me.eeshe.tempus.entity.Task;
import me.eeshe.tempus.request.CreateTaskRequest;
import me.eeshe.tempus.request.PatchTaskRequest;

public interface TaskService {

    List<Task> listTasks(long userId);

    Task getTask(long userId, long taskId);

    Optional<Task> getTask(long userId, String taskName, long projectId);

    Task createTask(CreateTaskRequest createTaskRequest);

    Task patchTask(long userId, long taskId, PatchTaskRequest patchTaskRequest);

    void deleteTask(long userId, long taskId);
}
