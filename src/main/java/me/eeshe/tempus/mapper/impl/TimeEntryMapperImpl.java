package me.eeshe.tempus.mapper.impl;

import org.springframework.stereotype.Component;

import me.eeshe.tempus.dto.CreateTimeEntryRequestDTO;
import me.eeshe.tempus.dto.PatchTimeEntryRequestDTO;
import me.eeshe.tempus.dto.TimeEntryDTO;
import me.eeshe.tempus.entity.Task;
import me.eeshe.tempus.entity.TimeEntry;
import me.eeshe.tempus.mapper.ProjectMapper;
import me.eeshe.tempus.mapper.TaskMapper;
import me.eeshe.tempus.mapper.TimeEntryMapper;
import me.eeshe.tempus.request.CreateTimeEntryRequest;
import me.eeshe.tempus.request.PatchTimeEntryRequest;
import me.eeshe.tempus.service.ProjectService;
import me.eeshe.tempus.service.TaskService;
import me.eeshe.tempus.service.UserService;

@Component
public class TimeEntryMapperImpl implements TimeEntryMapper {
    private final UserService userService;
    private final ProjectService projectService;
    private final TaskService taskService;
    private final ProjectMapper projectMapper;
    private final TaskMapper taskMapper;

    public TimeEntryMapperImpl(
            UserService userService,
            ProjectService projectService,
            TaskService taskService,
            ProjectMapper projectMapper,
            TaskMapper taskMapper) {
        this.userService = userService;
        this.projectService = projectService;
        this.taskService = taskService;
        this.projectMapper = projectMapper;
        this.taskMapper = taskMapper;
    }

    @Override
    public TimeEntryDTO toDTO(TimeEntry timeEntry) {
        return new TimeEntryDTO(
                timeEntry.getId(),
                timeEntry.getUser().getId(),
                projectMapper.toDTO(timeEntry.getProject()),
                taskMapper.toDTO(timeEntry.getTask()),
                timeEntry.getDescription(),
                timeEntry.isBillable(),
                timeEntry.getStartTime(),
                timeEntry.getEndTime(),
                timeEntry.getCreatedAt());
    }

    @Override
    public CreateTimeEntryRequest fromDTO(CreateTimeEntryRequestDTO createTimeEntryRequestDTO, long userId) {
        return new CreateTimeEntryRequest(
                userService.getUser(userId),
                projectService.getProject(userId, createTimeEntryRequestDTO.projectId()),
                resolveTask(userId, createTimeEntryRequestDTO.taskId()),
                createTimeEntryRequestDTO.description(),
                createTimeEntryRequestDTO.isBillable(),
                createTimeEntryRequestDTO.startTime(),
                createTimeEntryRequestDTO.endTime());
    }

    @Override
    public PatchTimeEntryRequest fromDTO(PatchTimeEntryRequestDTO patchTimeEntryRequestDTO, long userId) {
        return new PatchTimeEntryRequest(
                patchTimeEntryRequestDTO.projectId().map(id -> projectService.getProject(userId, id)),
                patchTimeEntryRequestDTO.taskId().map(id -> resolveTask(userId, id)),
                patchTimeEntryRequestDTO.description(),
                patchTimeEntryRequestDTO.isBillable(),
                patchTimeEntryRequestDTO.startTime(),
                patchTimeEntryRequestDTO.endTime());
    }

    private Task resolveTask(final long userId, final Long taskId) {
        return taskId != null ? taskService.getTask(userId, taskId) : null;
    }
}
