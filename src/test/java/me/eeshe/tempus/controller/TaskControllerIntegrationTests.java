package me.eeshe.tempus.controller;

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
    private static final String TASKS_PATH = "/api/v1/tasks";
    private static final String USERS_PATH = "/api/v1/users";
    private static final String PROJECTS_PATH = "/api/v1/projects";
    private final MockMvc mockMvc;

    @Autowired
    public TaskControllerIntegrationTests(MockMvc mockMvc) {
        this.mockMvc = mockMvc;
    }

    @Test
    public void testThatCreateTaskReturnsValidTask() throws Exception {
        createMockProject();

        mockMvc.perform(MockMvcRequestBuilders.post(TASKS_PATH)
                .content(generateCreateTaskJson())
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.jsonPath("$.id").isNumber())
                .andExpect(MockMvcResultMatchers.jsonPath("$.name").isNotEmpty())
                .andExpect(MockMvcResultMatchers.jsonPath("$.userId").isNumber())
                .andExpect(MockMvcResultMatchers.jsonPath("$.projectId").isNumber())
                .andExpect(MockMvcResultMatchers.jsonPath("$.createdAt").isNotEmpty());
    }

    @Test
    public void testThatCreateTaskReturnsHttp201Created() throws Exception {
        createMockProject();

        mockMvc.perform(MockMvcRequestBuilders.post(TASKS_PATH)
                .content(generateCreateTaskJson())
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
        createMockUser();

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
        createMockTask();

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
        createMockTask();

        mockMvc.perform(MockMvcRequestBuilders.get(TASKS_PATH + "/1"))
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
        createMockTask();

        mockMvc.perform(MockMvcRequestBuilders.patch(TASKS_PATH + "/1")
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
        createMockTask(); // Creates task with name MyTask
        final String json = generatePatchTaskJson(); // JSON contains name change to MyNewTaskName

        mockMvc.perform(MockMvcRequestBuilders.patch(TASKS_PATH + "/1")
                .content(json)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.jsonPath("$.name").value("MyNewTaskName"));
    }

    @Test
    public void testThatPatchTaskWithEmptyNameReturnsHttp400BadRequest() throws Exception {
        createMockTask();
        final String json = """
                {
                    "name": ""
                }
                    """;

        mockMvc.perform(MockMvcRequestBuilders.patch(TASKS_PATH + "/1")
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
        createMockTask();

        mockMvc.perform(MockMvcRequestBuilders.delete(TASKS_PATH + "/1"));

        mockMvc.perform(MockMvcRequestBuilders.get(TASKS_PATH))
                .andExpect(MockMvcResultMatchers.jsonPath("$").isEmpty());
    }

    @Test
    public void testThatDeleteTaskReturnsHttp204NoContent() throws Exception {
        createMockTask();

        mockMvc.perform(MockMvcRequestBuilders.delete(TASKS_PATH + "/1"))
                .andExpect(MockMvcResultMatchers.status().isNoContent());
    }

    private void createMockProject() throws Exception {
        createMockUser();

        mockMvc.perform(MockMvcRequestBuilders.post(PROJECTS_PATH)
                .content(generateCreateProjectJson())
                .contentType(MediaType.APPLICATION_JSON));
    }

    private void createMockUser() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.post(USERS_PATH)
                .content("""
                        {
                            "name": "MockUser"
                        }
                        """)
                .contentType(MediaType.APPLICATION_JSON));
    }

    private void createMockTask() throws Exception {
        createMockUser();

        mockMvc.perform(MockMvcRequestBuilders.post(PROJECTS_PATH)
                .content(generateCreateProjectJson())
                .contentType(MediaType.APPLICATION_JSON));

        mockMvc.perform(MockMvcRequestBuilders.post(TASKS_PATH)
                .content(generateCreateTaskJson())
                .contentType(MediaType.APPLICATION_JSON));
    }

    private String generateCreateTaskJson() {
        return """
                {
                    "name": "MyTask",
                    "userId": 1,
                    "projectId": 1
                }
                    """;
    }

    private String generatePatchTaskJson() {
        return """
                {
                    "name": "MyNewTaskName"
                }
                    """;
    }

    private String generateCreateProjectJson() {
        return """
                {
                    "name": "MyProject",
                    "userId": 1,
                    "isPrivate": false
                }
                    """;
    }
}
