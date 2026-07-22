package me.eeshe.tempus.service.impl;

import java.util.List;

import org.springframework.stereotype.Service;

import me.eeshe.tempus.entity.Task;
import me.eeshe.tempus.exception.TaskNotFoundException;
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
    public List<Task> listTasks() {
        return taskRepository.findAll();
    }

    @Override
    public Task getTask(long taskId) {
        return taskRepository.findById(taskId).orElseThrow(() -> new TaskNotFoundException(taskId));
    }

    @Override
    public Task createTask(CreateTaskRequest createTaskRequest) {
        return taskRepository.save(new Task(
                createTaskRequest.name(),
                createTaskRequest.user(),
                createTaskRequest.project()));
    }

    @Override
    public Task patchTask(long taskId, PatchTaskRequest patchTaskRequest) {
        final Task task = getTask(taskId);
        if (patchTaskRequest.name() != null) {
            task.setName(patchTaskRequest.name());
        }
        return taskRepository.save(task);
    }

    @Override
    public void deleteTask(long taskId) {
        taskRepository.deleteById(taskId);
    }

}
