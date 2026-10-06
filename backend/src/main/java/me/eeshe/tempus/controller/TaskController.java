package me.eeshe.tempus.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import me.eeshe.tempus.dto.CreateTaskRequestDTO;
import me.eeshe.tempus.dto.PatchTaskRequestDTO;
import me.eeshe.tempus.dto.TaskDTO;
import me.eeshe.tempus.entity.Task;
import me.eeshe.tempus.mapper.TaskMapper;
import me.eeshe.tempus.request.CreateTaskRequest;
import me.eeshe.tempus.request.PatchTaskRequest;
import me.eeshe.tempus.security.UserDetailsImpl;
import me.eeshe.tempus.service.TaskService;

@RestController
@RequestMapping(path = "api/v1/tasks")
public class TaskController {
    private final TaskService taskService;
    private final TaskMapper taskMapper;

    public TaskController(TaskService taskService, TaskMapper taskMapper) {
        this.taskService = taskService;
        this.taskMapper = taskMapper;
    }

    @GetMapping
    public ResponseEntity<List<TaskDTO>> getTasks(
            @AuthenticationPrincipal UserDetailsImpl userDetails) {
        final List<Task> tasks = taskService.listTasks(userDetails.getId());
        final List<TaskDTO> taskDTOs = tasks.stream().map(taskMapper::toDTO).toList();

        return ResponseEntity.ok(taskDTOs);
    }

    @GetMapping(path = "/{taskId}")
    public ResponseEntity<TaskDTO> getTask(
            @PathVariable long taskId,
            @AuthenticationPrincipal UserDetailsImpl userDetails) {
        final Task task = taskService.getTask(userDetails.getId(), taskId);
        final TaskDTO taskDTO = taskMapper.toDTO(task);

        return ResponseEntity.ok(taskDTO);
    }

    @PostMapping
    public ResponseEntity<TaskDTO> createTask(
            @Valid @RequestBody CreateTaskRequestDTO createTaskRequestDTO,
            @AuthenticationPrincipal UserDetailsImpl userDetails) {
        final CreateTaskRequest createTaskRequest = taskMapper.fromDTO(createTaskRequestDTO, userDetails.getId());
        final Task createdTask = taskService.createTask(createTaskRequest);
        final TaskDTO createdTaskDTO = taskMapper.toDTO(createdTask);

        return new ResponseEntity<>(createdTaskDTO, HttpStatus.CREATED);
    }

    @PatchMapping(path = "/{taskId}")
    public ResponseEntity<TaskDTO> patchTask(
            @PathVariable long taskId,
            @Valid @RequestBody PatchTaskRequestDTO patchTaskRequestDTO,
            @AuthenticationPrincipal UserDetailsImpl userDetails) {
        final PatchTaskRequest patchTaskRequest = taskMapper.fromDTO(patchTaskRequestDTO, userDetails.getId());
        final Task patchedTask = taskService.patchTask(userDetails.getId(), taskId, patchTaskRequest);
        final TaskDTO patchedTaskDTO = taskMapper.toDTO(patchedTask);

        return ResponseEntity.ok(patchedTaskDTO);
    }

    @DeleteMapping(path = "/{taskId}")
    public ResponseEntity<Void> deleteTask(
            @PathVariable long taskId,
            @AuthenticationPrincipal UserDetailsImpl userDetails) {
        taskService.deleteTask(userDetails.getId(), taskId);

        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}
