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
public class ProjectControllerIntegrationTests {
    private static final String PROJECTS_PATH = "/api/v1/projects";
    private static final String USERS_PATH = "/api/v1/users";
    private static final String CLIENTS_PATH = "/api/v1/clients";
    private final MockMvc mockMvc;

    @Autowired
    public ProjectControllerIntegrationTests(MockMvc mockMvc) {
        this.mockMvc = mockMvc;
    }

    @Test
    public void testThatCreateProjectReturnsValidProject() throws Exception {
        createMockUser();

        mockMvc.perform(MockMvcRequestBuilders.post(PROJECTS_PATH)
                .content(generateCreateProjectJson())
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.jsonPath("$.id").isNumber())
                .andExpect(MockMvcResultMatchers.jsonPath("$.name").isNotEmpty())
                .andExpect(MockMvcResultMatchers.jsonPath("$.userId").isNumber())
                .andExpect(MockMvcResultMatchers.jsonPath("$.isPrivate").isBoolean())
                .andExpect(MockMvcResultMatchers.jsonPath("$.createdAt").isNotEmpty());
    }

    @Test
    public void testThatCreateProjectReturnsHttp201Created() throws Exception {
        createMockUser();

        mockMvc.perform(MockMvcRequestBuilders.post(PROJECTS_PATH)
                .content(generateCreateProjectJson())
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isCreated());
    }

    @Test
    public void testThatCreateProjectWithEmptyNameReturnsHttp400BadRequest() throws Exception {
        final String json = """
                {
                    "name": "",
                    "userId": 1,
                    "isPrivate": false
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
                    "userId": 1,
                    "isPrivate": false
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
                    "name": "MyProject",
                    "isPrivate": false
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
                    "userId": 9999,
                    "isPrivate": false
                }
                    """;
        mockMvc.perform(MockMvcRequestBuilders.post(PROJECTS_PATH)
                .content(json)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isBadRequest());
    }

    @Test
    public void testThatCreateProjectWithNullIsPrivateReturnsHttp400BadRequest() throws Exception {
        final String json = """
                {
                    "name": "MyProject",
                    "userId": 1
                }
                    """;
        mockMvc.perform(MockMvcRequestBuilders.post(PROJECTS_PATH)
                .content(json)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isBadRequest());
    }

    @Test
    public void testThatCreateProjectWithUnexistentClientIdReturnsHttp400BadRequest() throws Exception {
        createMockUser();

        final String json = """
                {
                    "name": "MyProject",
                    "userId": 1,
                    "isPrivate": false,
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
        createMockProject();

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
        createMockProject();

        mockMvc.perform(MockMvcRequestBuilders.get(PROJECTS_PATH + "/1"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.id").isNumber())
                .andExpect(MockMvcResultMatchers.jsonPath("$.name").isNotEmpty())
                .andExpect(MockMvcResultMatchers.jsonPath("$.userId").isNumber())
                .andExpect(MockMvcResultMatchers.jsonPath("$.isPrivate").isBoolean())
                .andExpect(MockMvcResultMatchers.jsonPath("$.createdAt").isNotEmpty());
    }

    @Test
    public void testThatGetUnexistentProjectReturnsHttp400BadRequest() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get(PROJECTS_PATH + "/9999"))
                .andExpect(MockMvcResultMatchers.status().isBadRequest());
    }

    @Test
    public void testThatPatchProjectReturnsValidProject() throws Exception {
        createMockProject();

        mockMvc.perform(MockMvcRequestBuilders.patch(PROJECTS_PATH + "/1")
                .content(generatePatchProjectJson())
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.jsonPath("$.id").isNumber())
                .andExpect(MockMvcResultMatchers.jsonPath("$.name").isNotEmpty())
                .andExpect(MockMvcResultMatchers.jsonPath("$.userId").isNumber())
                .andExpect(MockMvcResultMatchers.jsonPath("$.isPrivate").isBoolean())
                .andExpect(MockMvcResultMatchers.jsonPath("$.createdAt").isNotEmpty());
    }

    @Test
    public void testThatPatchProjectChangesProjectName() throws Exception {
        createMockProject();
        final String json = generatePatchProjectJson();

        mockMvc.perform(MockMvcRequestBuilders.patch(PROJECTS_PATH + "/1")
                .content(json)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.jsonPath("$.name").value("MyNewProjectName"));
    }

    @Test
    public void testThatPatchProjectWithIsPrivate() throws Exception {
        createMockProject();

        final String json = """
                {
                    "isPrivate": true
                }
                    """;
        mockMvc.perform(MockMvcRequestBuilders.patch(PROJECTS_PATH + "/1")
                .content(json)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.jsonPath("$.isPrivate").value(true));
    }

    @Test
    public void testThatPatchProjectWithClientId() throws Exception {
        createMockClient();
        final String json = """
                {
                    "clientId": 1
                }
                    """;

        mockMvc.perform(MockMvcRequestBuilders.patch(PROJECTS_PATH + "/1")
                .content(json)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.jsonPath("$.clientId").value(1));
    }

    @Test
    public void testThatPatchProjectWithEmptyNameReturnsHttp400BadRequest() throws Exception {
        createMockProject();
        final String json = """
                {
                    "name": ""
                }
                    """;

        mockMvc.perform(MockMvcRequestBuilders.patch(PROJECTS_PATH + "/1")
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
        createMockProject();

        mockMvc.perform(MockMvcRequestBuilders.delete(PROJECTS_PATH + "/1"));

        mockMvc.perform(MockMvcRequestBuilders.get(PROJECTS_PATH))
                .andExpect(MockMvcResultMatchers.jsonPath("$").isEmpty());
    }

    @Test
    public void testThatDeleteProjectReturnsHttp204NoContent() throws Exception {
        createMockProject();

        mockMvc.perform(MockMvcRequestBuilders.delete(PROJECTS_PATH + "/1"))
                .andExpect(MockMvcResultMatchers.status().isNoContent());
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

    private void createMockClient() throws Exception {
        createMockUser();

        mockMvc.perform(MockMvcRequestBuilders.post(CLIENTS_PATH)
                .content("""
                        {
                            "name": "MyClient",
                            "userId": 1,
                            "hourlyRate": 25.0
                        }
                        """)
                .contentType(MediaType.APPLICATION_JSON));

        createMockProject();
    }

    private void createMockProject() throws Exception {
        createMockUser();

        mockMvc.perform(MockMvcRequestBuilders.post(PROJECTS_PATH)
                .content(generateCreateProjectJson())
                .contentType(MediaType.APPLICATION_JSON));
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

    private String generatePatchProjectJson() {
        return """
                {
                    "name": "MyNewProjectName"
                }
                    """;
    }
}
