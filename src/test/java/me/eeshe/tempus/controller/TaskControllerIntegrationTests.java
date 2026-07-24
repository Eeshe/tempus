package me.eeshe.tempus.controller;

import static me.eeshe.tempus.testutil.TestEntityFactory.*;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import jakarta.transaction.Transactional;

@SpringBootTest
@ExtendWith(SpringExtension.class)
@AutoConfigureMockMvc
@Transactional
public class TaskControllerIntegrationTests {
    private final MockMvc mockMvc;

    @Autowired
    public TaskControllerIntegrationTests(MockMvc mockMvc) {
        this.mockMvc = mockMvc;
    }

    @Test
    public void testThatCreateTaskReturnsValidTask() throws Exception {
        long userId = createUser(mockMvc);
        long projectId = createProject(mockMvc, userId);

        mockMvc.perform(MockMvcRequestBuilders.post(TASKS_PATH)
                .content("""
                        {
                            "name": "MyTask",
                            "userId": %d,
                            "projectId": %d
                        }
                        """.formatted(userId, projectId))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.jsonPath("$.id").isNumber())
                .andExpect(MockMvcResultMatchers.jsonPath("$.name").isNotEmpty())
                .andExpect(MockMvcResultMatchers.jsonPath("$.userId").isNumber())
                .andExpect(MockMvcResultMatchers.jsonPath("$.projectId").isNumber())
                .andExpect(MockMvcResultMatchers.jsonPath("$.createdAt").isNotEmpty());
    }

    @Test
    public void testThatCreateTaskReturnsHttp201Created() throws Exception {
        long userId = createUser(mockMvc);
        long projectId = createProject(mockMvc, userId);

        mockMvc.perform(MockMvcRequestBuilders.post(TASKS_PATH)
                .content("""
                        {
                            "name": "MyTask",
                            "userId": %d,
                            "projectId": %d
                        }
                        """.formatted(userId, projectId))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isCreated());
    }

    @Test
    public void testThatCreateTaskWithEmptyNameReturnsHttp400BadRequest() throws Exception {
        final String json = """
                {
                    "name": "",
                    "userId": 1,
                    "projectId": 1
                }
                    """;
        mockMvc.perform(MockMvcRequestBuilders.post(TASKS_PATH)
                .content(json)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isBadRequest());
    }

    @Test
    public void testThatCreateTaskWithNullNameReturnsHttp400BadRequest() throws Exception {
        final String json = """
                {
                    "userId": 1,
                    "projectId": 1
                }
                    """;
        mockMvc.perform(MockMvcRequestBuilders.post(TASKS_PATH)
                .content(json)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isBadRequest());
    }

    @Test
    public void testThatCreateTaskWithNullUserIdReturnsHttp400BadRequest() throws Exception {
        final String json = """
                {
                    "name": "MyTask",
                    "projectId": 1
                }
                    """;
        mockMvc.perform(MockMvcRequestBuilders.post(TASKS_PATH)
                .content(json)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isBadRequest());
    }

    @Test
    public void testThatCreateTaskWithNullProjectIdReturnsHttp400BadRequest() throws Exception {
        final String json = """
                {
                    "name": "MyTask",
                    "userId": 1
                }
                    """;
        mockMvc.perform(MockMvcRequestBuilders.post(TASKS_PATH)
                .content(json)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isBadRequest());
    }

    @Test
    public void testThatCreateTaskWithUnexistentUserIdReturnsHttp400BadRequest() throws Exception {
        final String json = """
                {
                    "name": "MyTask",
                    "userId": 9999,
                    "projectId": 1
                }
                    """;
        mockMvc.perform(MockMvcRequestBuilders.post(TASKS_PATH)
                .content(json)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isBadRequest());
    }

    @Test
    public void testThatCreateTaskWithUnexistentProjectIdReturnsHttp400BadRequest() throws Exception {
        createUser(mockMvc);

        final String json = """
                {
                    "name": "MyTask",
                    "userId": 1,
                    "projectId": 9999
                }
                    """;
        mockMvc.perform(MockMvcRequestBuilders.post(TASKS_PATH)
                .content(json)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isBadRequest());
    }

    @Test
    public void testThatListTasksReturnsNotEmptyList() throws Exception {
        long userId = createUser(mockMvc);
        long projectId = createProject(mockMvc, userId);
        createTask(mockMvc, userId, projectId);

        mockMvc.perform(MockMvcRequestBuilders.get(TASKS_PATH))
                .andExpect(MockMvcResultMatchers.jsonPath("$").isNotEmpty());
    }

    @Test
    public void testThatListTasksReturnsEmptyList() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get(TASKS_PATH))
                .andExpect(MockMvcResultMatchers.jsonPath("$").isEmpty());
    }

    @Test
    public void testThatGetTaskReturnsValidTask() throws Exception {
        long userId = createUser(mockMvc);
        long projectId = createProject(mockMvc, userId);
        long taskId = createTask(mockMvc, userId, projectId);

        mockMvc.perform(MockMvcRequestBuilders.get(TASKS_PATH + "/" + taskId))
                .andExpect(MockMvcResultMatchers.jsonPath("$.id").isNumber())
                .andExpect(MockMvcResultMatchers.jsonPath("$.name").isNotEmpty())
                .andExpect(MockMvcResultMatchers.jsonPath("$.userId").isNumber())
                .andExpect(MockMvcResultMatchers.jsonPath("$.projectId").isNumber())
                .andExpect(MockMvcResultMatchers.jsonPath("$.createdAt").isNotEmpty());
    }

    @Test
    public void testThatGetUnexistentTaskReturnsHttp400BadRequest() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get(TASKS_PATH + "/9999"))
                .andExpect(MockMvcResultMatchers.status().isBadRequest());
    }

    @Test
    public void testThatPatchTaskReturnsValidTask() throws Exception {
        long userId = createUser(mockMvc);
        long projectId = createProject(mockMvc, userId);
        long taskId = createTask(mockMvc, userId, projectId);

        mockMvc.perform(MockMvcRequestBuilders.patch(TASKS_PATH + "/" + taskId)
                .content(generatePatchTaskJson())
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.jsonPath("$.id").isNumber())
                .andExpect(MockMvcResultMatchers.jsonPath("$.name").isNotEmpty())
                .andExpect(MockMvcResultMatchers.jsonPath("$.userId").isNumber())
                .andExpect(MockMvcResultMatchers.jsonPath("$.projectId").isNumber())
                .andExpect(MockMvcResultMatchers.jsonPath("$.createdAt").isNotEmpty());
    }

    @Test
    public void testThatPatchTaskChangesTaskName() throws Exception {
        long userId = createUser(mockMvc); // Creates user with name MockUser
        long projectId = createProject(mockMvc, userId); // Creates project with name MyProject
        long taskId = createTask(mockMvc, userId, projectId); // Creates task with name MyTask
        final String json = generatePatchTaskJson(); // JSON contains name change to MyNewTaskName

        mockMvc.perform(MockMvcRequestBuilders.patch(TASKS_PATH + "/" + taskId)
                .content(json)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.jsonPath("$.name").value("MyNewTaskName"));
    }

    @Test
    public void testThatPatchTaskWithEmptyNameReturnsHttp400BadRequest() throws Exception {
        long userId = createUser(mockMvc);
        long projectId = createProject(mockMvc, userId);
        long taskId = createTask(mockMvc, userId, projectId);
        final String json = """
                {
                    "name": ""
                }
                    """;

        mockMvc.perform(MockMvcRequestBuilders.patch(TASKS_PATH + "/" + taskId)
                .content(json)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isBadRequest());
    }

    @Test
    public void testThatPatchUnexistentTaskReturnsHttp400BadRequest() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.patch(TASKS_PATH + "/1")
                .content(generatePatchTaskJson())
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isBadRequest());
    }

    @Test
    public void testThatDeleteTaskReturnsEmptyList() throws Exception {
        long userId = createUser(mockMvc);
        long projectId = createProject(mockMvc, userId);
        long taskId = createTask(mockMvc, userId, projectId);

        mockMvc.perform(MockMvcRequestBuilders.delete(TASKS_PATH + "/" + taskId));

        mockMvc.perform(MockMvcRequestBuilders.get(TASKS_PATH))
                .andExpect(MockMvcResultMatchers.jsonPath("$").isEmpty());
    }

    @Test
    public void testThatDeleteTaskReturnsHttp204NoContent() throws Exception {
        long userId = createUser(mockMvc);
        long projectId = createProject(mockMvc, userId);
        long taskId = createTask(mockMvc, userId, projectId);

        mockMvc.perform(MockMvcRequestBuilders.delete(TASKS_PATH + "/" + taskId))
                .andExpect(MockMvcResultMatchers.status().isNoContent());
    }

    private String generatePatchTaskJson() {
        return """
                {
                    "name": "MyNewTaskName"
                }
                    """;
    }
}
