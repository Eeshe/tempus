package me.eeshe.tempus.mapper.impl;

import org.openapitools.jackson.nullable.JsonNullable;
import org.springframework.stereotype.Component;

import me.eeshe.tempus.dto.CreateProjectRequestDTO;
import me.eeshe.tempus.dto.PatchProjectRequestDTO;
import me.eeshe.tempus.dto.ProjectDTO;
import me.eeshe.tempus.entity.Client;
import me.eeshe.tempus.entity.Project;
import me.eeshe.tempus.mapper.ProjectMapper;
import me.eeshe.tempus.request.CreateProjectRequest;
import me.eeshe.tempus.request.PatchProjectRequest;
import me.eeshe.tempus.service.ClientService;
import me.eeshe.tempus.service.UserService;

@Component
public class ProjectMapperImpl implements ProjectMapper {
    private final UserService userService;
    private final ClientService clientService;

    public ProjectMapperImpl(UserService userService, ClientService clientService) {
        this.userService = userService;
        this.clientService = clientService;
    }

    @Override
    public ProjectDTO toDTO(Project project) {
        return new ProjectDTO(
                project.getId(),
                project.getName(),
                project.getUser().getId(),
                project.isPrivate(),
                project.getClientId(),
                project.getCreatedAt());
    }

    @Override
    public CreateProjectRequest fromDTO(CreateProjectRequestDTO createProjectRequestDTO, long userId) {
        return new CreateProjectRequest(
                createProjectRequestDTO.name(),
                userService.getUser(userId),
                createProjectRequestDTO.isPrivate(),
                resolveClient(createProjectRequestDTO.clientId()));
    }

    @Override
    public PatchProjectRequest fromDTO(PatchProjectRequestDTO patchProjectRequestDTO) {
        return new PatchProjectRequest(
                patchProjectRequestDTO.name(),
                patchProjectRequestDTO.isPrivate(),
                JsonNullable.of(resolveClient(patchProjectRequestDTO.clientId().orElse(null))));
    }

    private Client resolveClient(Long clientId) {
        return clientId != null ? clientService.getClient(clientId) : null;
    }
}
