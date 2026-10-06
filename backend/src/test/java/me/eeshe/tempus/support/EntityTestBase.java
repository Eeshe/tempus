package me.eeshe.tempus.support;

import java.time.Instant;

import org.springframework.test.util.ReflectionTestUtils;

import me.eeshe.tempus.entity.Client;
import me.eeshe.tempus.entity.Project;
import me.eeshe.tempus.entity.Task;
import me.eeshe.tempus.entity.User;

public abstract class EntityTestBase {
    protected static final long USER_ID = 1L;
    protected static final Instant CREATED_AT = Instant.parse("2026-01-01T10:00:00Z");

    protected static final long CLIENT_ID = 10L;
    protected static final long SECOND_CLIENT_ID = 11L;

    protected static final long PROJECT_ID = 20L;
    protected static final long SECOND_PROJECT_ID = 21L;

    protected static final long TASK_ID = 300L;
    protected static final long SECOND_TASK_ID = 301L;

    protected static User createUser(long userId) {
        final User user = new User("MyUser", "MyPassword");
        ReflectionTestUtils.setField(user, "id", userId);

        return user;
    }

    protected static Client createClient() {
        return createClient(CLIENT_ID, "MyClient", CREATED_AT);
    }

    protected static Client createSecondClient() {
        return createClient(SECOND_CLIENT_ID, "MySecondClient", CREATED_AT);
    }

    protected static Client createClient(long id, String name, Instant createdAt) {
        final Client client = new Client(name, createUser(USER_ID));
        ReflectionTestUtils.setField(client, "id", id);
        ReflectionTestUtils.setField(client, "createdAt", createdAt);

        return client;
    }

    protected static Project createProject() {
        return createProject(PROJECT_ID, "MyProject", CREATED_AT);
    }

    protected static Project createSecondProject() {
        return createProject(SECOND_PROJECT_ID, "MySecondProject", CREATED_AT);
    }

    protected static Project createProject(long id, String name, Instant createdAt) {
        final Project project = new Project(name, createUser(USER_ID), null, null);
        ReflectionTestUtils.setField(project, "id", id);
        ReflectionTestUtils.setField(project, "createdAt", createdAt);

        return project;
    }

    protected static Task createTask() {
        return createTask(TASK_ID, "MyTask", CREATED_AT, createProject());
    }

    protected static Task createSecondTask() {
        return createTask(SECOND_TASK_ID, "MySecondTask", CREATED_AT, createProject());
    }

    protected static Task createTask(long id, String name, Instant createdAt, Project project) {
        final Task task = new Task(name, createUser(USER_ID), project);
        ReflectionTestUtils.setField(task, "id", id);
        ReflectionTestUtils.setField(task, "createdAt", createdAt);

        return task;
    }
}
