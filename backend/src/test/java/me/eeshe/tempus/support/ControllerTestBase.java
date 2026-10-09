package me.eeshe.tempus.support;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.assertj.MockMvcTester;
import org.springframework.test.web.servlet.request.RequestPostProcessor;

import me.eeshe.tempus.config.SecurityConfig;
import me.eeshe.tempus.dto.ClientDTO;
import me.eeshe.tempus.dto.ProjectDTO;
import me.eeshe.tempus.dto.TaskDTO;
import me.eeshe.tempus.dto.UserDTO;
import me.eeshe.tempus.repository.UserRepository;
import me.eeshe.tempus.security.RestAuthenticationEntryPoint;
import me.eeshe.tempus.security.UserDetailsImpl;

@Import({ SecurityConfig.class, RestAuthenticationEntryPoint.class })
public abstract class ControllerTestBase extends EntityTestBase {

    @Autowired
    protected MockMvcTester mockMvc;

    @MockitoBean
    protected UserRepository userRepository;

    protected RequestPostProcessor createPrincipalWithCsrf(long userId) {
        return request -> createCsrf().postProcessRequest(createPrincipal(userId).postProcessRequest(request));
    }

    protected RequestPostProcessor createPrincipal(long userId) {
        return user(new UserDetailsImpl(createUser(userId)));
    }

    protected RequestPostProcessor createCsrf() {
        return csrf();
    }

    protected static UserDTO createTestUserDTO(long userId, Instant createdAt) {
        return new UserDTO(userId, "MyUser", createdAt);
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

    protected static ProjectDTO createProjectDTO(
            long id, String name, Instant createdAt, BigDecimal hourlyRate, ClientDTO client) {
        return createProjectDTO(id, name, createdAt, hourlyRate, client, false);
    }

    protected static ProjectDTO createProjectDTO(
            long id, String name, Instant createdAt, BigDecimal hourlyRate, ClientDTO client,
            boolean isArchived) {
        return new ProjectDTO(id, name, USER_ID, hourlyRate, List.of(), client, createdAt, isArchived);
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
        return createProjectDTOJson(id, name, createdAt, hourlyRate, clientJson, false);
    }

    protected static String createProjectDTOJson(
            long id, String name, Instant createdAt, BigDecimal hourlyRate, String clientJson,
            boolean isArchived) {
        return """
                {
                    "id": %s,
                    "name": "%s",
                    "userId": %s,
                    "hourlyRate": %s,
                    "tasks": [],
                    "client": %s,
                    "createdAt": "%s",
                    "isArchived": %s
                }""".formatted(id, name, USER_ID, hourlyRate, clientJson == null ? "null" : clientJson, createdAt,
                isArchived);
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
