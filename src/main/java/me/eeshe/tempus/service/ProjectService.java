package me.eeshe.tempus.service;

import java.util.List;

import me.eeshe.tempus.entity.Project;
import me.eeshe.tempus.request.CreateProjectRequest;
import me.eeshe.tempus.request.PatchProjectRequest;

public interface ProjectService {

    List<Project> listProjects();

    Project getProject(long projectId);

    Project createProject(CreateProjectRequest createProjectRequest);

    Project patchProject(long projectId, PatchProjectRequest patchProjectRequest);

    void deleteProject(long projectId);
}
