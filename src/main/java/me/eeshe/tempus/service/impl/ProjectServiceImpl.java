package me.eeshe.tempus.service.impl;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import me.eeshe.tempus.entity.Project;
import me.eeshe.tempus.exception.ProjectNotFoundException;
import me.eeshe.tempus.exception.UserProjectAlreadyExistsException;
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
    public List<Project> listProjects(long userId) {
        return projectRepository.findByUserId(userId);
    }

    @Override
    public Project getProject(long userId, long projectId) {
        return projectRepository.findByIdAndUserId(projectId, userId)
                .orElseThrow(() -> new ProjectNotFoundException(projectId));
    }

    @Override
    public Optional<Project> getProject(long userId, String projectName) {
        return projectRepository.findByUserIdAndName(userId, projectName);
    }

    @Override
    public Project createProject(CreateProjectRequest createProjectRequest) {
        final long userId = createProjectRequest.user().getId();
        final String projectName = createProjectRequest.name();
        projectRepository.findByUserIdAndName(userId, projectName).ifPresent(project -> {
            throw new UserProjectAlreadyExistsException(userId, projectName);
        });
        return projectRepository.save(new Project(
                createProjectRequest.name(),
                createProjectRequest.user(),
                createProjectRequest.hourlyRate(),
                createProjectRequest.client()));
    }

    @Override
    public Project patchProject(long userId, long projectId, PatchProjectRequest patchProjectRequest) {
        final Project project = getProject(userId, projectId);
        if (patchProjectRequest.name() != null) {
            project.setName(patchProjectRequest.name());
        }
        patchProjectRequest.hourlyRate().ifPresent(project::setHourlyRate);
        patchProjectRequest.client().ifPresent(project::setClient);

        return projectRepository.save(project);
    }

    @Override
    public void deleteProject(long userId, long projectId) {
        getProject(userId, projectId);

        projectRepository.deleteById(projectId);
    }
}
