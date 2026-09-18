package me.eeshe.tempus.service;

import java.util.List;

import me.eeshe.tempus.entity.Project;
import me.eeshe.tempus.request.CreateProjectRequest;
import me.eeshe.tempus.request.PatchProjectRequest;

public interface ProjectService {

    List<Project> listProjects(long userId);

    Project getProject(long userId, long projectId);

    Project createProject(CreateProjectRequest createProjectRequest);

    Project patchProject(long userId, long projectId, PatchProjectRequest patchProjectRequest);

    void deleteProject(long userId, long projectId);
}
