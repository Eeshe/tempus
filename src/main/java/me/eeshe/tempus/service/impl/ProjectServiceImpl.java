package me.eeshe.tempus.service.impl;

import java.util.List;

import org.springframework.stereotype.Service;

import me.eeshe.tempus.entity.Project;
import me.eeshe.tempus.exception.ProjectNotFoundException;
import me.eeshe.tempus.repository.ProjectRepository;
import me.eeshe.tempus.request.CreateProjectRequest;
import me.eeshe.tempus.request.PatchProjectRequest;
import me.eeshe.tempus.service.ProjectService;

@Service
public class ProjectServiceImpl implements ProjectService {
    private final ProjectRepository projectRepository;

    public ProjectServiceImpl(ProjectRepository projectRepository) {
        this.projectRepository = projectRepository;
    }

    @Override
    public List<Project> listProjects() {
        return projectRepository.findAll();
    }

    @Override
    public Project getProject(long projectId) {
        return projectRepository.findById(projectId).orElseThrow(() -> new ProjectNotFoundException(projectId));
    }

    @Override
    public Project createProject(CreateProjectRequest createProjectRequest) {
        return projectRepository.save(new Project(
                createProjectRequest.name(),
                createProjectRequest.user(),
                createProjectRequest.isPrivate(),
                createProjectRequest.client()));
    }

    @Override
    public Project patchProject(long projectId, PatchProjectRequest patchProjectRequest) {
        final Project project = getProject(projectId);
        if (patchProjectRequest.name() != null) {
            project.setName(patchProjectRequest.name());
        }
        patchProjectRequest.isPrivate().ifPresent(project::setPrivate);
        patchProjectRequest.client().ifPresent(project::setClient);

        return projectRepository.save(project);
    }

    @Override
    public void deleteProject(long projectId) {
        projectRepository.deleteById(projectId);
    }
}
