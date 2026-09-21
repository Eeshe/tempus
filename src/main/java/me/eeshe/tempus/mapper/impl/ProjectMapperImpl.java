package me.eeshe.tempus.mapper.impl;

import org.openapitools.jackson.nullable.JsonNullable;
import org.springframework.stereotype.Component;

import me.eeshe.tempus.dto.ClientDTO;
import me.eeshe.tempus.dto.CreateProjectRequestDTO;
import me.eeshe.tempus.dto.PatchProjectRequestDTO;
import me.eeshe.tempus.dto.ProjectDTO;
import me.eeshe.tempus.entity.Client;
import me.eeshe.tempus.entity.Project;
import me.eeshe.tempus.mapper.ClientMapper;
import me.eeshe.tempus.mapper.ProjectMapper;
import me.eeshe.tempus.mapper.TaskMapper;
import me.eeshe.tempus.request.CreateProjectRequest;
import me.eeshe.tempus.request.PatchProjectRequest;
import me.eeshe.tempus.service.ClientService;
import me.eeshe.tempus.service.UserService;

@Component
public class ProjectMapperImpl implements ProjectMapper {
    private final UserService userService;
    private final ClientService clientService;
    private final TaskMapper taskMapper;
    private final ClientMapper clientMapper;

    public ProjectMapperImpl(
            UserService userService,
            ClientService clientService,
            TaskMapper taskMapper,
            ClientMapper clientMapper) {
        this.userService = userService;
        this.clientService = clientService;
        this.taskMapper = taskMapper;
        this.clientMapper = clientMapper;
    }

    @Override
    public ProjectDTO toDTO(Project project) {
        final ClientDTO clientDTO = project.getClient() == null ? null : clientMapper.toDTO(project.getClient());
        return new ProjectDTO(
                project.getId(),
                project.getName(),
                project.getUser().getId(),
                project.getHourlyRate(),
                project.getTasks().stream().map(taskMapper::toDTO).toList(),
                clientDTO,
                project.getCreatedAt());
    }

    @Override
    public CreateProjectRequest fromDTO(CreateProjectRequestDTO createProjectRequestDTO, long userId) {
        return new CreateProjectRequest(
                createProjectRequestDTO.name(),
                userService.getUser(userId),
                createProjectRequestDTO.hourlyRate(),
                resolveClient(userId, createProjectRequestDTO.clientId()));
    }

    @Override
    public PatchProjectRequest fromDTO(PatchProjectRequestDTO patchProjectRequestDTO, long userId) {
        return new PatchProjectRequest(
                patchProjectRequestDTO.name(),
                patchProjectRequestDTO.hourlyRate(),
                JsonNullable.of(resolveClient(userId, patchProjectRequestDTO.clientId().orElse(null))));
    }

    private Client resolveClient(long userId, Long clientId) {
        return clientId != null ? clientService.getClient(userId, clientId) : null;
    }
}
