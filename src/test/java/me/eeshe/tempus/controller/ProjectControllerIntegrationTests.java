package me.eeshe.tempus.controller;

import static me.eeshe.tempus.testutil.TestEntityFactory.PROJECTS_PATH;
import static me.eeshe.tempus.testutil.TestEntityFactory.TASKS_PATH;
import static me.eeshe.tempus.testutil.TestEntityFactory.TIME_ENTRIES_PATH;
import static me.eeshe.tempus.testutil.TestEntityFactory.createClient;
import static me.eeshe.tempus.testutil.TestEntityFactory.createProject;
import static me.eeshe.tempus.testutil.TestEntityFactory.createTask;
import static me.eeshe.tempus.testutil.TestEntityFactory.createTimeEntry;
import static me.eeshe.tempus.testutil.TestEntityFactory.createUser;

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

@SpringBootTest
@ExtendWith(SpringExtension.class)
@AutoConfigureMockMvc
public class ProjectControllerIntegrationTests extends BaseControllerTest {
    private final MockMvc mockMvc;

    @Autowired
    public ProjectControllerIntegrationTests(MockMvc mockMvc) {
        this.mockMvc = mockMvc;
    }

    @Test
    public void testThatCreateProjectReturnsValidProject() throws Exception {
        createUser(mockMvc);

        mockMvc.perform(MockMvcRequestBuilders.post(PROJECTS_PATH)
                .content("""
                        {
                            "name": "MyProject",
                            "userId": 1
                        }
                        """)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.jsonPath("$.id").isNumber())
                .andExpect(MockMvcResultMatchers.jsonPath("$.name").isNotEmpty())
                .andExpect(MockMvcResultMatchers.jsonPath("$.userId").isNumber())
                .andExpect(MockMvcResultMatchers.jsonPath("$.createdAt").isNotEmpty());
    }

    @Test
    public void testThatCreateProjectReturnsHttp201Created() throws Exception {
        createUser(mockMvc);

        mockMvc.perform(MockMvcRequestBuilders.post(PROJECTS_PATH)
                .content("""
                        {
                            "name": "MyProject",
                            "userId": 1
                        }
                        """)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isCreated());
    }

    @Test
    public void testThatCreateProjectWithEmptyNameReturnsHttp400BadRequest() throws Exception {
        final String json = """
                {
                    "name": "",
                    "userId": 1
                }
                    """;
        mockMvc.perform(MockMvcRequestBuilders.post(PROJECTS_PATH)
                .content(json)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isBadRequest());
    }

    @Test
    public void testThatCreateProjectWithNullNameReturnsHttp400BadRequest() throws Exception {
        final String json = """
                {
                    "userId": 1
                }
                    """;
        mockMvc.perform(MockMvcRequestBuilders.post(PROJECTS_PATH)
                .content(json)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isBadRequest());
    }

    @Test
    public void testThatCreateProjectWithNullUserIdReturnsHttp400BadRequest() throws Exception {
        final String json = """
                {
                    "name": "MyProject"
                }
                    """;
        mockMvc.perform(MockMvcRequestBuilders.post(PROJECTS_PATH)
                .content(json)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isBadRequest());
    }

    @Test
    public void testThatCreateProjectWithUnexistentUserIdReturnsHttp400BadRequest() throws Exception {
        final String json = """
                {
                    "name": "MyProject",
                    "userId": 9999
                }
                    """;
        mockMvc.perform(MockMvcRequestBuilders.post(PROJECTS_PATH)
                .content(json)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isBadRequest());
    }

    @Test
    public void testThatCreateProjectWithUnexistentClientIdReturnsHttp400BadRequest() throws Exception {
        createUser(mockMvc);

        final String json = """
                {
                    "name": "MyProject",
                    "userId": 1,
                    "clientId": 9999
                }
                    """;
        mockMvc.perform(MockMvcRequestBuilders.post(PROJECTS_PATH)
                .content(json)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isBadRequest());
    }

    @Test
    public void testThatListProjectsReturnsNotEmptyList() throws Exception {
        long userId = createUser(mockMvc);
        createProject(mockMvc, userId);

        mockMvc.perform(MockMvcRequestBuilders.get(PROJECTS_PATH))
                .andExpect(MockMvcResultMatchers.jsonPath("$").isNotEmpty());
    }

    @Test
    public void testThatListProjectsReturnsEmptyList() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get(PROJECTS_PATH))
                .andExpect(MockMvcResultMatchers.jsonPath("$").isEmpty());
    }

    @Test
    public void testThatGetProjectReturnsValidProject() throws Exception {
        long userId = createUser(mockMvc);
        long projectId = createProject(mockMvc, userId);

        mockMvc.perform(MockMvcRequestBuilders.get(PROJECTS_PATH + "/" + projectId))
                .andExpect(MockMvcResultMatchers.jsonPath("$.id").isNumber())
                .andExpect(MockMvcResultMatchers.jsonPath("$.name").isNotEmpty())
                .andExpect(MockMvcResultMatchers.jsonPath("$.userId").isNumber())
                .andExpect(MockMvcResultMatchers.jsonPath("$.createdAt").isNotEmpty());
    }

    @Test
    public void testThatGetUnexistentProjectReturnsHttp400BadRequest() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get(PROJECTS_PATH + "/9999"))
                .andExpect(MockMvcResultMatchers.status().isBadRequest());
    }

    @Test
    public void testThatPatchProjectReturnsValidProject() throws Exception {
        long userId = createUser(mockMvc);
        long projectId = createProject(mockMvc, userId);

        mockMvc.perform(MockMvcRequestBuilders.patch(PROJECTS_PATH + "/" + projectId)
                .content(generatePatchProjectJson())
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.jsonPath("$.id").isNumber())
                .andExpect(MockMvcResultMatchers.jsonPath("$.name").isNotEmpty())
                .andExpect(MockMvcResultMatchers.jsonPath("$.userId").isNumber())
                .andExpect(MockMvcResultMatchers.jsonPath("$.createdAt").isNotEmpty());
    }

    @Test
    public void testThatPatchProjectChangesProjectName() throws Exception {
        long userId = createUser(mockMvc);
        long projectId = createProject(mockMvc, userId);
        final String json = generatePatchProjectJson();

        mockMvc.perform(MockMvcRequestBuilders.patch(PROJECTS_PATH + "/" + projectId)
                .content(json)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.jsonPath("$.name").value("MyNewProjectName"));
    }

    @Test
    public void testThatPatchProjectWithClientId() throws Exception {
        long userId = createUser(mockMvc);
        long clientId = createClient(mockMvc, userId);
        long projectId = createProject(mockMvc, userId);
        final String json = """
                {
                    "clientId": %d
                }
                    """.formatted(clientId);

        mockMvc.perform(MockMvcRequestBuilders.patch(PROJECTS_PATH + "/" + projectId)
                .content(json)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.jsonPath("$.clientId").value(clientId));
    }

    @Test
    public void testThatPatchProjectWithEmptyNameReturnsHttp400BadRequest() throws Exception {
        long userId = createUser(mockMvc);
        long projectId = createProject(mockMvc, userId);
        final String json = """
                {
                    "name": ""
                }
                    """;

        mockMvc.perform(MockMvcRequestBuilders.patch(PROJECTS_PATH + "/" + projectId)
                .content(json)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isBadRequest());
    }

    @Test
    public void testThatPatchUnexistentProjectReturnsHttp400BadRequest() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.patch(PROJECTS_PATH + "/1")
                .content(generatePatchProjectJson())
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isBadRequest());
    }

    @Test
    public void testThatDeleteProjectReturnsEmptyList() throws Exception {
        long userId = createUser(mockMvc);
        long projectId = createProject(mockMvc, userId);

        mockMvc.perform(MockMvcRequestBuilders.delete(PROJECTS_PATH + "/" + projectId));

        mockMvc.perform(MockMvcRequestBuilders.get(PROJECTS_PATH))
                .andExpect(MockMvcResultMatchers.jsonPath("$").isEmpty());
    }

    @Test
    public void testThatDeleteProjectReturnsHttp204NoContent() throws Exception {
        long userId = createUser(mockMvc);
        long projectId = createProject(mockMvc, userId);

        mockMvc.perform(MockMvcRequestBuilders.delete(PROJECTS_PATH + "/" + projectId))
                .andExpect(MockMvcResultMatchers.status().isNoContent());
    }

    @Test
    public void testThatDeleteProjectCascadesIntoTask() throws Exception {
        final long userId = createUser(mockMvc);
        final long projectId = createProject(mockMvc, userId);
        final long taskId = createTask(mockMvc, userId, projectId);

        mockMvc.perform(MockMvcRequestBuilders.get(TASKS_PATH + "/" + taskId))
                .andExpect(MockMvcResultMatchers.jsonPath("$.projectId").value(projectId));

        mockMvc.perform(MockMvcRequestBuilders.delete(PROJECTS_PATH + "/" + userId));
        mockMvc.perform(MockMvcRequestBuilders.get(PROJECTS_PATH + "/" + taskId))
                .andExpect(MockMvcResultMatchers.status().isBadRequest());
    }

    @Test
    public void testThatDeleteProjectCascadesIntoTimeEntries() throws Exception {
        final long userId = createUser(mockMvc);
        final long projectId = createProject(mockMvc, userId);
        final long timeEntryId = createTimeEntry(mockMvc, userId, projectId, false);

        mockMvc.perform(MockMvcRequestBuilders.get(TIME_ENTRIES_PATH + "/" + timeEntryId))
                .andExpect(MockMvcResultMatchers.jsonPath("$.projectId").value(projectId));

        mockMvc.perform(MockMvcRequestBuilders.delete(PROJECTS_PATH + "/" + userId));
        mockMvc.perform(MockMvcRequestBuilders.get(TIME_ENTRIES_PATH + "/" + timeEntryId))
                .andExpect(MockMvcResultMatchers.status().isBadRequest());
    }

    private String generatePatchProjectJson() {
        return """
                {
                    "name": "MyNewProjectName"
                }
                    """;
    }
}
