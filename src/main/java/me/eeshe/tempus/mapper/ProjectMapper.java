package me.eeshe.tempus.mapper;

import me.eeshe.tempus.dto.CreateProjectRequestDTO;
import me.eeshe.tempus.dto.PatchProjectRequestDTO;
import me.eeshe.tempus.dto.ProjectDTO;
import me.eeshe.tempus.entity.Project;
import me.eeshe.tempus.request.CreateProjectRequest;
import me.eeshe.tempus.request.PatchProjectRequest;

public interface ProjectMapper {

    ProjectDTO toDTO(Project project);

    CreateProjectRequest fromDTO(CreateProjectRequestDTO createProjectRequestDTO, long userId);

    PatchProjectRequest fromDTO(PatchProjectRequestDTO patchProjectRequestDTO, long userId);
}
