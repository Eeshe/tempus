package me.eeshe.tempus.mapper.impl;

import org.springframework.stereotype.Component;

import me.eeshe.tempus.dto.CreateTimeEntryRequestDTO;
import me.eeshe.tempus.dto.PatchTimeEntryRequestDTO;
import me.eeshe.tempus.dto.TimeEntryDTO;
import me.eeshe.tempus.entity.Group;
import me.eeshe.tempus.entity.Task;
import me.eeshe.tempus.entity.TimeEntry;
import me.eeshe.tempus.mapper.ProjectMapper;
import me.eeshe.tempus.mapper.TaskMapper;
import me.eeshe.tempus.mapper.TimeEntryMapper;
import me.eeshe.tempus.request.CreateTimeEntryRequest;
import me.eeshe.tempus.request.PatchTimeEntryRequest;
import me.eeshe.tempus.service.GroupService;
import me.eeshe.tempus.service.ProjectService;
import me.eeshe.tempus.service.TaskService;
import me.eeshe.tempus.service.UserService;

@Component
public class TimeEntryMapperImpl implements TimeEntryMapper {
    private final GroupService groupService;
    private final UserService userService;
    private final ProjectService projectService;
    private final TaskService taskService;
    private final ProjectMapper projectMapper;
    private final TaskMapper taskMapper;

    public TimeEntryMapperImpl(
            GroupService groupService,
            UserService userService,
            ProjectService projectService,
            TaskService taskService,
            ProjectMapper projectMapper,
            TaskMapper taskMapper) {
        this.groupService = groupService;
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
                timeEntry.getGroupId(),
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
    public CreateTimeEntryRequest fromDTO(CreateTimeEntryRequestDTO createTimeEntryRequestDTO) {
        return new CreateTimeEntryRequest(
                resolveGroup(createTimeEntryRequestDTO.groupId()),
                userService.getUser(createTimeEntryRequestDTO.userId()),
                projectService.getProject(createTimeEntryRequestDTO.projectId()),
                resolveTask(createTimeEntryRequestDTO.taskId()),
                createTimeEntryRequestDTO.description(),
                createTimeEntryRequestDTO.isBillable(),
                createTimeEntryRequestDTO.startTime(),
                createTimeEntryRequestDTO.endTime());
    }

    @Override
    public PatchTimeEntryRequest fromDTO(PatchTimeEntryRequestDTO patchTimeEntryRequestDTO) {
        return new PatchTimeEntryRequest(
                patchTimeEntryRequestDTO.groupId().map(this::resolveGroup),
                patchTimeEntryRequestDTO.projectId().map(projectService::getProject),
                patchTimeEntryRequestDTO.taskId().map(this::resolveTask),
                patchTimeEntryRequestDTO.description(),
                patchTimeEntryRequestDTO.isBillable(),
                patchTimeEntryRequestDTO.startTime(),
                patchTimeEntryRequestDTO.endTime());
    }

    private Group resolveGroup(final Long groupId) {
        return groupId != null ? groupService.getGroup(groupId) : null;
    }

    private Task resolveTask(final Long taskId) {
        return taskId != null ? taskService.getTask(taskId) : null;
    }
}
