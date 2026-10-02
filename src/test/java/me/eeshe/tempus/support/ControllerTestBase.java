package me.eeshe.tempus.support;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.assertj.MockMvcTester;
import org.springframework.test.web.servlet.request.RequestPostProcessor;

import me.eeshe.tempus.config.SecurityConfig;
import me.eeshe.tempus.dto.ClientDTO;
import me.eeshe.tempus.dto.ProjectDTO;
import me.eeshe.tempus.dto.TaskDTO;
import me.eeshe.tempus.dto.UserDTO;
import me.eeshe.tempus.entity.Client;
import me.eeshe.tempus.entity.Project;
import me.eeshe.tempus.entity.Task;
import me.eeshe.tempus.entity.User;
import me.eeshe.tempus.repository.UserRepository;
import me.eeshe.tempus.security.RestAuthenticationEntryPoint;
import me.eeshe.tempus.security.UserDetailsImpl;

@Import({ SecurityConfig.class, RestAuthenticationEntryPoint.class })
public abstract class ControllerTestBase {

    protected static final long USER_ID = 1L;
    protected static final Instant CREATED_AT = Instant.parse("2026-01-01T10:00:00Z");

    protected static final long CLIENT_ID = 10L;
    protected static final long SECOND_CLIENT_ID = 11L;

    protected static final long PROJECT_ID = 20L;
    protected static final long SECOND_PROJECT_ID = 21L;

    protected static final long TASK_ID = 300L;
    protected static final long SECOND_TASK_ID = 301L;

    @Autowired
    protected MockMvcTester mockMvc;

    @MockitoBean
    protected UserRepository userRepository;

    protected RequestPostProcessor createPrincipal(long userId) {
        return user(new UserDetailsImpl(createUser(userId)));
    }

    protected static User createUser(long userId) {
        final User user = new User("MyUser", "MyPassword");
        ReflectionTestUtils.setField(user, "id", userId);

        return user;
    }

    protected static UserDTO createTestUserDTO(long userId, Instant createdAt) {
        return new UserDTO(userId, "MyUser", createdAt);
    }

    protected static Client createClient(long id, String name, Instant createdAt) {
        final Client client = new Client(name, createUser(USER_ID));
        ReflectionTestUtils.setField(client, "id", id);
        ReflectionTestUtils.setField(client, "createdAt", createdAt);

        return client;
    }

    protected static Client createClient() {
        return createClient(CLIENT_ID, "MyClient", CREATED_AT);
    }

    protected static Client createSecondClient() {
        return createClient(SECOND_CLIENT_ID, "MySecondClient", CREATED_AT);
    }

    protected static ClientDTO createClientDTO(long id, String name, Instant createdAt) {
        return new ClientDTO(id, name, USER_ID, createdAt);
    }

    protected static ClientDTO createClientDTO() {
        return createClientDTO(CLIENT_ID, "MyClient", CREATED_AT);
    }

    protected static ClientDTO createSecondClientDTO() {
        return createClientDTO(SECOND_CLIENT_ID, "MySecondClient", CREATED_AT);
    }

    protected static String createClientDTOJson(long id, String name, Instant createdAt) {
        return """
                {
                    "id": %s,
                    "name": "%s",
                    "userId": %s,
                    "createdAt": "%s"
                }""".formatted(id, name, USER_ID, createdAt);
    }

    protected static String createClientDTOJson() {
        return createClientDTOJson(CLIENT_ID, "MyClient", CREATED_AT);
    }

    protected static String createSecondClientDTOJson() {
        return createClientDTOJson(SECOND_CLIENT_ID, "MySecondClient", CREATED_AT);
    }

    protected static Project createProject(long id, String name, Instant createdAt) {
        final Project project = new Project(name, createUser(USER_ID), null, null);
        ReflectionTestUtils.setField(project, "id", id);
        ReflectionTestUtils.setField(project, "createdAt", createdAt);

        return project;
    }

    protected static Project createProject() {
        return createProject(PROJECT_ID, "MyProject", CREATED_AT);
    }

    protected static Project createSecondProject() {
        return createProject(SECOND_PROJECT_ID, "MySecondProject", CREATED_AT);
    }

    protected static ProjectDTO createProjectDTO(
            long id, String name, Instant createdAt, BigDecimal hourlyRate, ClientDTO client) {
        return new ProjectDTO(id, name, USER_ID, hourlyRate, List.of(), client, createdAt);
    }

    protected static ProjectDTO createProjectDTO() {
        return createProjectDTO(PROJECT_ID, "MyProject", CREATED_AT, null, null);
    }

    protected static ProjectDTO createProjectDTO(BigDecimal hourlyRate, ClientDTO client) {
        return createProjectDTO(PROJECT_ID, "MyProject", CREATED_AT, hourlyRate, client);
    }

    protected static ProjectDTO createSecondProjectDTO() {
        return createProjectDTO(SECOND_PROJECT_ID, "MySecondProject", CREATED_AT, null, null);
    }

    protected static String createProjectDTOJson(
            long id, String name, Instant createdAt, BigDecimal hourlyRate, String clientJson) {
        return """
                {
                    "id": %s,
                    "name": "%s",
                    "userId": %s,
                    "hourlyRate": %s,
                    "tasks": [],
                    "client": %s,
                    "createdAt": "%s"
                }""".formatted(id, name, USER_ID, hourlyRate, clientJson == null ? "null" : clientJson, createdAt);
    }

    protected static String createProjectDTOJson() {
        return createProjectDTOJson(PROJECT_ID, "MyProject", CREATED_AT, null, null);
    }

    protected static String createProjectDTOJson(BigDecimal hourlyRate, String clientJson) {
        return createProjectDTOJson(PROJECT_ID, "MyProject", CREATED_AT, hourlyRate, clientJson);
    }

    protected static String createSecondProjectDTOJson() {
        return createProjectDTOJson(SECOND_PROJECT_ID, "MySecondProject", CREATED_AT, null, null);
    }

    protected static Task createTask(long id, String name, Instant createdAt, Project project) {
        final Task task = new Task(name, createUser(USER_ID), project);
        ReflectionTestUtils.setField(task, "id", id);
        ReflectionTestUtils.setField(task, "createdAt", createdAt);

        return task;
    }

    protected static Task createTask() {
        return createTask(TASK_ID, "MyTask", CREATED_AT, createProject());
    }

    protected static Task createSecondTask() {
        return createTask(SECOND_TASK_ID, "MySecondTask", CREATED_AT, createProject());
    }

    protected static TaskDTO createTaskDTO(long id, String name, long projectId, Instant createdAt) {
        return new TaskDTO(id, name, USER_ID, projectId, createdAt);
    }

    protected static TaskDTO createTaskDTO() {
        return createTaskDTO(TASK_ID, "MyTask", PROJECT_ID, CREATED_AT);
    }

    protected static TaskDTO createSecondTaskDTO() {
        return createTaskDTO(SECOND_TASK_ID, "MySecondTask", PROJECT_ID, CREATED_AT);
    }

    protected static String createTaskDTOJson(long id, String name, long projectId, Instant createdAt) {
        return """
                {
                    "id": %s,
                    "name": "%s",
                    "userId": %s,
                    "projectId": %s,
                    "createdAt": "%s"
                }""".formatted(id, name, USER_ID, projectId, createdAt);
    }

    protected static String createTaskDTOJson() {
        return createTaskDTOJson(TASK_ID, "MyTask", PROJECT_ID, CREATED_AT);
    }

    protected static String createSecondTaskDTOJson() {
        return createTaskDTOJson(SECOND_TASK_ID, "MySecondTask", PROJECT_ID, CREATED_AT);
    }
}
