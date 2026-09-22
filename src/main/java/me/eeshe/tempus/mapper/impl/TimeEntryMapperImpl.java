package me.eeshe.tempus.mapper.impl;

import java.time.DateTimeException;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;
import java.util.Optional;

import org.springframework.stereotype.Component;

import me.eeshe.tempus.dto.CreateTimeEntryRequestDTO;
import me.eeshe.tempus.dto.PatchTimeEntryRequestDTO;
import me.eeshe.tempus.dto.TimeEntryDTO;
import me.eeshe.tempus.entity.Client;
import me.eeshe.tempus.entity.Project;
import me.eeshe.tempus.entity.Task;
import me.eeshe.tempus.entity.TimeEntry;
import me.eeshe.tempus.entity.User;
import me.eeshe.tempus.exception.CSVTimeEntryImportException;
import me.eeshe.tempus.mapper.ProjectMapper;
import me.eeshe.tempus.mapper.TaskMapper;
import me.eeshe.tempus.mapper.TimeEntryMapper;
import me.eeshe.tempus.model.CSVTimeEntry;
import me.eeshe.tempus.request.CreateClientRequest;
import me.eeshe.tempus.request.CreateProjectRequest;
import me.eeshe.tempus.request.CreateTaskRequest;
import me.eeshe.tempus.request.CreateTimeEntryRequest;
import me.eeshe.tempus.request.PatchTimeEntryRequest;
import me.eeshe.tempus.service.ClientService;
import me.eeshe.tempus.service.ProjectService;
import me.eeshe.tempus.service.TaskService;
import me.eeshe.tempus.service.UserService;

@Component
public class TimeEntryMapperImpl implements TimeEntryMapper {
    private static final DateTimeFormatter TIME_FORMATTER = new DateTimeFormatterBuilder()
            .appendPattern("HH:mm").toFormatter();

    private final UserService userService;
    private final ProjectService projectService;
    private final TaskService taskService;
    private final ClientService clientService;
    private final ProjectMapper projectMapper;
    private final TaskMapper taskMapper;

    public TimeEntryMapperImpl(
            UserService userService,
            ProjectService projectService,
            TaskService taskService,
            ClientService clientService,
            ProjectMapper projectMapper,
            TaskMapper taskMapper) {
        this.userService = userService;
        this.projectService = projectService;
        this.taskService = taskService;
        this.clientService = clientService;
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
    public CreateTimeEntryRequest fromCSVTimeEntry(CSVTimeEntry csvTimeEntry, long userId) {
        final User user = userService.getUser(userId);

        final LocalDate date = csvTimeEntry.getDate();
        final ZoneId zoneId = resolveZoneId(csvTimeEntry.getTimezone());
        final Instant startTime = parseInstant(date, csvTimeEntry.getStart(), zoneId)
                .orElseThrow(() -> new CSVTimeEntryImportException(date, "Start time not provided"));
        Instant endTime = parseInstant(date, csvTimeEntry.getEnd(), zoneId).orElse(startTime);
        if (endTime.isBefore(startTime)) {
            endTime = endTime.plus(1, ChronoUnit.DAYS);
        }

        final Client client = resolveOrCreateClient(csvTimeEntry, user);
        final Project project = resolveOrCreateProject(csvTimeEntry, client, user);
        final Task task = resolveOrCreateTask(csvTimeEntry, project, user);

        return new CreateTimeEntryRequest(
                user,
                project,
                task,
                csvTimeEntry.getDescription(),
                csvTimeEntry.isBillable(),
                startTime,
                endTime);
    }

    private Client resolveOrCreateClient(CSVTimeEntry csvTimeEntry, User user) {
        final String clientName = csvTimeEntry.getClient();
        if (clientName == null || clientName.isBlank()) {
            return null;
        }
        return clientService.getClient(user.getId(), clientName).orElseGet(() -> {
            return clientService.createClient(new CreateClientRequest(
                    clientName,
                    user));
        });
    }

    private Project resolveOrCreateProject(CSVTimeEntry csvTimeEntry, Client client, User user) {
        String projectName = csvTimeEntry.getProject();
        if (projectName == null || projectName.isBlank()) {
            projectName = "Unnamed Project";
        }
        final String finalProjectName = projectName;
        return projectService.getProject(user.getId(), projectName).orElseGet(() -> {
            return projectService.createProject(new CreateProjectRequest(
                    finalProjectName,
                    user,
                    csvTimeEntry.getHourlyRate(),
                    client));
        });
    }

    private Task resolveOrCreateTask(CSVTimeEntry csvTimeEntry, Project project, User user) {
        final String taskName = csvTimeEntry.getTask();
        if (taskName == null || taskName.isBlank()) {
            return null;
        }
        return taskService.getTask(user.getId(), taskName, project.getId()).orElseGet(() -> {
            return taskService.createTask(new CreateTaskRequest(
                    taskName,
                    user,
                    project));
        });
    }

    private Optional<Instant> parseInstant(LocalDate date, String time, ZoneId zoneId) {
        try {
            final LocalTime localTime = LocalTime.parse(time, TIME_FORMATTER);

            return Optional.of(LocalDateTime.of(date, localTime).atZone(zoneId).toInstant());
        } catch (DateTimeParseException e) {
            return Optional.empty();
        }
    }

    private ZoneId resolveZoneId(String timezone) {
        if (timezone == null) {
            return ZoneOffset.UTC;
        }
        try {
            return ZoneId.of(timezone);
        } catch (DateTimeException e) {
            return ZoneOffset.UTC;
        }
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
