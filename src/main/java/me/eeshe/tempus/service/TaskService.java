package me.eeshe.tempus.service;

import java.util.List;

import me.eeshe.tempus.entity.Task;
import me.eeshe.tempus.request.CreateTaskRequest;
import me.eeshe.tempus.request.PatchTaskRequest;

public interface TaskService {

    List<Task> listTasks();

    Task getTask(long taskId);

    Task createTask(CreateTaskRequest createTaskRequest);

    Task patchTask(long taskId, PatchTaskRequest patchTaskRequest);

    void deleteTask(long taskId);
}
