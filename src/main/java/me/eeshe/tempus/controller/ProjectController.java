package me.eeshe.tempus.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import me.eeshe.tempus.dto.CreateProjectRequestDTO;
import me.eeshe.tempus.dto.PatchProjectRequestDTO;
import me.eeshe.tempus.dto.ProjectDTO;
import me.eeshe.tempus.entity.Project;
import me.eeshe.tempus.mapper.ProjectMapper;
import me.eeshe.tempus.request.CreateProjectRequest;
import me.eeshe.tempus.request.PatchProjectRequest;
import me.eeshe.tempus.security.UserDetailsImpl;
import me.eeshe.tempus.service.ProjectService;

@RestController
@RequestMapping(path = "api/v1/projects")
public class ProjectController {
    private final ProjectService projectService;
    private final ProjectMapper projectMapper;

    public ProjectController(ProjectService projectService, ProjectMapper projectMapper) {
        this.projectService = projectService;
        this.projectMapper = projectMapper;
    }

    @GetMapping
    public ResponseEntity<List<ProjectDTO>> listProjects() {
        final List<Project> projects = projectService.listProjects();
        final List<ProjectDTO> projectDTOs = projects.stream().map(projectMapper::toDTO).toList();

        return ResponseEntity.ok(projectDTOs);
    }

    @GetMapping(path = "/{projectId}")
    public ResponseEntity<ProjectDTO> getProject(@PathVariable long projectId) {
        final Project project = projectService.getProject(projectId);
        final ProjectDTO projectDTO = projectMapper.toDTO(project);

        return ResponseEntity.ok(projectDTO);
    }

    @PostMapping
    public ResponseEntity<ProjectDTO> createProject(
            @Valid @RequestBody CreateProjectRequestDTO createProjectRequestDTO,
            @AuthenticationPrincipal UserDetails userDetails) {
        final long userId = ((UserDetailsImpl) userDetails).getId();
        final CreateProjectRequest createProjectRequest = projectMapper.fromDTO(createProjectRequestDTO, userId);
        final Project createdProject = projectService.createProject(createProjectRequest);
        final ProjectDTO createdProjectDTO = projectMapper.toDTO(createdProject);

        return new ResponseEntity<>(createdProjectDTO, HttpStatus.CREATED);

    }

    @PatchMapping(path = "/{projectId}")
    public ResponseEntity<ProjectDTO> patchProject(
            @PathVariable long projectId,
            @Valid @RequestBody PatchProjectRequestDTO patchProjectRequestDTO) {
        final PatchProjectRequest patchProjectRequest = projectMapper.fromDTO(patchProjectRequestDTO);
        final Project patchedProject = projectService.patchProject(projectId, patchProjectRequest);
        final ProjectDTO patchedProjectDTO = projectMapper.toDTO(patchedProject);

        return ResponseEntity.ok(patchedProjectDTO);
    }

    @DeleteMapping(path = "/{projectId}")
    public ResponseEntity<Void> deleteProject(@PathVariable long projectId) {
        projectService.deleteProject(projectId);

        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}
