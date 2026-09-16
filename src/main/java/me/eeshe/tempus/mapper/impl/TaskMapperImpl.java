package me.eeshe.tempus.mapper.impl;

import org.springframework.stereotype.Component;

import me.eeshe.tempus.dto.CreateTaskRequestDTO;
import me.eeshe.tempus.dto.PatchTaskRequestDTO;
import me.eeshe.tempus.dto.TaskDTO;
import me.eeshe.tempus.entity.Task;
import me.eeshe.tempus.mapper.TaskMapper;
import me.eeshe.tempus.request.CreateTaskRequest;
import me.eeshe.tempus.request.PatchTaskRequest;
import me.eeshe.tempus.service.ProjectService;
import me.eeshe.tempus.service.UserService;

@Component
public class TaskMapperImpl implements TaskMapper {
    private final UserService userService;
    private final ProjectService projectService;

    public TaskMapperImpl(
            UserService userService,
            ProjectService projectService) {
        this.userService = userService;
        this.projectService = projectService;
    }

    @Override
    public TaskDTO toDTO(Task task) {
        if (task == null) {
            return null;
        }
        return new TaskDTO(
                task.getId(),
                task.getName(),
                task.getUser().getId(),
                task.getProject().getId(),
                task.getCreatedAt());
    }

    @Override
    public CreateTaskRequest fromDTO(CreateTaskRequestDTO createTaskRequestDTO, long userId) {
        return new CreateTaskRequest(
                createTaskRequestDTO.name(),
                userService.getUser(userId),
                projectService.getProject(createTaskRequestDTO.projectId()));
    }

    @Override
    public PatchTaskRequest fromDTO(PatchTaskRequestDTO patchTaskRequestDTO) {
        return new PatchTaskRequest(patchTaskRequestDTO.name());
    }

}
