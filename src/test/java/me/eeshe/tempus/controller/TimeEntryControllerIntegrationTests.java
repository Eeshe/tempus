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
public class TimeEntryControllerIntegrationTests {
    private static final String TIME_ENTRIES_PATH = "/api/v1/time-entries";
    private static final String USERS_PATH = "/api/v1/users";
    private static final String PROJECTS_PATH = "/api/v1/projects";
    private static final String GROUPS_PATH = "/api/v1/groups";
    private final MockMvc mockMvc;

    @Autowired
    public TimeEntryControllerIntegrationTests(MockMvc mockMvc) {
        this.mockMvc = mockMvc;
    }

    @Test
    public void testThatCreateTimeEntryReturnsValidTimeEntry() throws Exception {
        createMockProject();

        mockMvc.perform(MockMvcRequestBuilders.post(TIME_ENTRIES_PATH)
                .content(generateCreateTimeEntryJson())
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.jsonPath("$.id").isNumber())
                .andExpect(MockMvcResultMatchers.jsonPath("$.userId").isNumber())
                .andExpect(MockMvcResultMatchers.jsonPath("$.projectId").isNumber())
                .andExpect(MockMvcResultMatchers.jsonPath("$.isBillable").isBoolean())
                .andExpect(MockMvcResultMatchers.jsonPath("$.createdAt").isNotEmpty());
    }

    @Test
    public void testThatCreateTimeEntryReturnsHttp201Created() throws Exception {
        createMockProject();

        mockMvc.perform(MockMvcRequestBuilders.post(TIME_ENTRIES_PATH)
                .content(generateCreateTimeEntryJson())
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isCreated());
    }

    @Test
    public void testThatCreateTimeEntryWithNullUserIdReturnsHttp400BadRequest() throws Exception {
        final String json = """
                {
                    "projectId": 1,
                    "isBillable": true
                }
                    """;
        mockMvc.perform(MockMvcRequestBuilders.post(TIME_ENTRIES_PATH)
                .content(json)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isBadRequest());
    }

    @Test
    public void testThatCreateTimeEntryWithNullProjectIdReturnsHttp400BadRequest() throws Exception {
        final String json = """
                {
                    "userId": 1,
                    "isBillable": true
                }
                    """;
        mockMvc.perform(MockMvcRequestBuilders.post(TIME_ENTRIES_PATH)
                .content(json)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isBadRequest());
    }

    @Test
    public void testThatCreateTimeEntryWithNullIsBillableReturnsHttp400BadRequest() throws Exception {
        final String json = """
                {
                    "userId": 1,
                    "projectId": 1
                }
                    """;
        mockMvc.perform(MockMvcRequestBuilders.post(TIME_ENTRIES_PATH)
                .content(json)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isBadRequest());
    }

    @Test
    public void testThatCreateTimeEntryWithUnexistentUserIdReturnsHttp400BadRequest() throws Exception {
        final String json = """
                {
                    "userId": 9999,
                    "projectId": 1,
                    "isBillable": true
                }
                    """;
        mockMvc.perform(MockMvcRequestBuilders.post(TIME_ENTRIES_PATH)
                .content(json)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isBadRequest());
    }

    @Test
    public void testThatCreateTimeEntryWithUnexistentProjectIdReturnsHttp400BadRequest() throws Exception {
        createMockUser();

        final String json = """
                {
                    "userId": 1,
                    "projectId": 9999,
                    "isBillable": true
                }
                    """;
        mockMvc.perform(MockMvcRequestBuilders.post(TIME_ENTRIES_PATH)
                .content(json)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isBadRequest());
    }

    @Test
    public void testThatCreateTimeEntryWithUnexistentGroupIdReturnsHttp400BadRequest() throws Exception {
        createMockProject();

        final String json = """
                {
                    "groupId": 9999,
                    "userId": 1,
                    "projectId": 1,
                    "isBillable": true
                }
                    """;
        mockMvc.perform(MockMvcRequestBuilders.post(TIME_ENTRIES_PATH)
                .content(json)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isBadRequest());
    }

    @Test
    public void testThatCreateTimeEntryWithUnexistentTaskIdReturnsHttp400BadRequest() throws Exception {
        createMockProject();

        final String json = """
                {
                    "taskId": 9999,
                    "userId": 1,
                    "projectId": 1,
                    "isBillable": true
                }
                    """;
        mockMvc.perform(MockMvcRequestBuilders.post(TIME_ENTRIES_PATH)
                .content(json)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isBadRequest());
    }

    @Test
    public void testThatListTimeEntriesReturnsNotEmptyList() throws Exception {
        createMockTimeEntry();

        mockMvc.perform(MockMvcRequestBuilders.get(TIME_ENTRIES_PATH))
                .andExpect(MockMvcResultMatchers.jsonPath("$").isNotEmpty());
    }

    @Test
    public void testThatListTimeEntriesReturnsEmptyList() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get(TIME_ENTRIES_PATH))
                .andExpect(MockMvcResultMatchers.jsonPath("$").isEmpty());
    }

    @Test
    public void testThatGetTimeEntryReturnsValidTimeEntry() throws Exception {
        createMockTimeEntry();

        mockMvc.perform(MockMvcRequestBuilders.get(TIME_ENTRIES_PATH + "/1"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.id").isNumber())
                .andExpect(MockMvcResultMatchers.jsonPath("$.userId").isNumber())
                .andExpect(MockMvcResultMatchers.jsonPath("$.projectId").isNumber())
                .andExpect(MockMvcResultMatchers.jsonPath("$.isBillable").isBoolean())
                .andExpect(MockMvcResultMatchers.jsonPath("$.createdAt").isNotEmpty());
    }

    @Test
    public void testThatGetUnexistentTimeEntryReturnsHttp400BadRequest() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get(TIME_ENTRIES_PATH + "/9999"))
                .andExpect(MockMvcResultMatchers.status().isBadRequest());
    }

    @Test
    public void testThatPatchTimeEntryReturnsValidTimeEntry() throws Exception {
        createMockTimeEntry();

        mockMvc.perform(MockMvcRequestBuilders.patch(TIME_ENTRIES_PATH + "/1")
                .content(generatePatchTimeEntryJson())
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.jsonPath("$.id").isNumber())
                .andExpect(MockMvcResultMatchers.jsonPath("$.userId").isNumber())
                .andExpect(MockMvcResultMatchers.jsonPath("$.projectId").isNumber())
                .andExpect(MockMvcResultMatchers.jsonPath("$.isBillable").isBoolean())
                .andExpect(MockMvcResultMatchers.jsonPath("$.createdAt").isNotEmpty());
    }

    @Test
    public void testThatPatchTimeEntryChangesDescription() throws Exception {
        createMockTimeEntry(); // Creates time entry with no description

        final String json = generatePatchTimeEntryJson(); // JSON contains description change to NewDescription

        mockMvc.perform(MockMvcRequestBuilders.patch(TIME_ENTRIES_PATH + "/1")
                .content(json)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.jsonPath("$.description").value("NewDescription"));
    }

    @Test
    public void testThatPatchTimeEntryChangesIsBillable() throws Exception {
        createMockTimeEntry(); // Creates time entry with isBillable true

        final String json = """
                {
                    "projectId": 1,
                    "taskId": null,
                    "isBillable": false
                }
                    """;
        mockMvc.perform(MockMvcRequestBuilders.patch(TIME_ENTRIES_PATH + "/1")
                .content(json)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.jsonPath("$.isBillable").value(false));
    }

    @Test
    public void testThatPatchTimeEntryWithGroupId() throws Exception {
        createMockTimeEntry();

        mockMvc.perform(MockMvcRequestBuilders.post(GROUPS_PATH)
                .content("""
                        {
                            "name": "MyGroup"
                        }
                        """)
                .contentType(MediaType.APPLICATION_JSON));

        final String json = """
                {
                    "groupId": 1,
                    "projectId": 1,
                    "taskId": null,
                    "isBillable": true
                }
                    """;
        mockMvc.perform(MockMvcRequestBuilders.patch(TIME_ENTRIES_PATH + "/1")
                .content(json)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.jsonPath("$.groupId").value(1));
    }

    @Test
    public void testThatPatchTimeEntryWithNullProjectIdReturnsHttp400BadRequest() throws Exception {
        createMockTimeEntry();

        final String json = """
                {
                    "projectId": null,
                    "taskId": null,
                    "isBillable": true
                }
                    """;
        mockMvc.perform(MockMvcRequestBuilders.patch(TIME_ENTRIES_PATH + "/1")
                .content(json)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isBadRequest());
    }

    @Test
    public void testThatPatchTimeEntryWithNullIsBillableReturnsHttp400BadRequest() throws Exception {
        createMockTimeEntry();

        final String json = """
                {
                    "projectId": 1,
                    "taskId": null,
                    "isBillable": null
                }
                    """;
        mockMvc.perform(MockMvcRequestBuilders.patch(TIME_ENTRIES_PATH + "/1")
                .content(json)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isBadRequest());
    }

    @Test
    public void testThatPatchUnexistentTimeEntryReturnsHttp400BadRequest() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.patch(TIME_ENTRIES_PATH + "/1")
                .content(generatePatchTimeEntryJson())
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isBadRequest());
    }

    @Test
    public void testThatDeleteTimeEntryReturnsEmptyList() throws Exception {
        createMockTimeEntry();

        mockMvc.perform(MockMvcRequestBuilders.delete(TIME_ENTRIES_PATH + "/1"));

        mockMvc.perform(MockMvcRequestBuilders.get(TIME_ENTRIES_PATH))
                .andExpect(MockMvcResultMatchers.jsonPath("$").isEmpty());
    }

    @Test
    public void testThatDeleteTimeEntryReturnsHttp204NoContent() throws Exception {
        createMockTimeEntry();

        mockMvc.perform(MockMvcRequestBuilders.delete(TIME_ENTRIES_PATH + "/1"))
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

    private void createMockProject() throws Exception {
        createMockUser();

        mockMvc.perform(MockMvcRequestBuilders.post(PROJECTS_PATH)
                .content(generateCreateProjectJson())
                .contentType(MediaType.APPLICATION_JSON));
    }

    private void createMockTimeEntry() throws Exception {
        createMockProject();

        mockMvc.perform(MockMvcRequestBuilders.post(TIME_ENTRIES_PATH)
                .content(generateCreateTimeEntryJson())
                .contentType(MediaType.APPLICATION_JSON));
    }

    private String generateCreateTimeEntryJson() {
        return """
                {
                    "userId": 1,
                    "projectId": 1,
                    "isBillable": true
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

    private String generatePatchTimeEntryJson() {
        return """
                {
                    "description": "NewDescription",
                    "projectId": 1,
                    "taskId": null,
                    "isBillable": true
                }
                    """;
    }
}
