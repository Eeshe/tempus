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
public class TimeEntryControllerIntegrationTests {
    private final MockMvc mockMvc;

    @Autowired
    public TimeEntryControllerIntegrationTests(MockMvc mockMvc) {
        this.mockMvc = mockMvc;
    }

    @Test
    public void testThatCreateTimeEntryReturnsValidTimeEntry() throws Exception {
        long userId = createUser(mockMvc);
        long projectId = createProject(mockMvc, userId);

        mockMvc.perform(MockMvcRequestBuilders.post(TIME_ENTRIES_PATH)
                .content("""
                        {
                            "userId": %d,
                            "projectId": %d,
                            "isBillable": true
                        }
                        """.formatted(userId, projectId))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.jsonPath("$.id").isNumber())
                .andExpect(MockMvcResultMatchers.jsonPath("$.userId").isNumber())
                .andExpect(MockMvcResultMatchers.jsonPath("$.projectId").isNumber())
                .andExpect(MockMvcResultMatchers.jsonPath("$.isBillable").isBoolean())
                .andExpect(MockMvcResultMatchers.jsonPath("$.createdAt").isNotEmpty());
    }

    @Test
    public void testThatCreateTimeEntryReturnsHttp201Created() throws Exception {
        long userId = createUser(mockMvc);
        long projectId = createProject(mockMvc, userId);

        mockMvc.perform(MockMvcRequestBuilders.post(TIME_ENTRIES_PATH)
                .content("""
                        {
                            "userId": %d,
                            "projectId": %d,
                            "isBillable": true
                        }
                        """.formatted(userId, projectId))
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
        createUser(mockMvc);

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
        long userId = createUser(mockMvc);
        createProject(mockMvc, userId);

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
        long userId = createUser(mockMvc);
        createProject(mockMvc, userId);

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
        long userId = createUser(mockMvc);
        long projectId = createProject(mockMvc, userId);
        createTimeEntry(mockMvc, userId, projectId, true);

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
        long userId = createUser(mockMvc);
        long projectId = createProject(mockMvc, userId);
        long timeEntryId = createTimeEntry(mockMvc, userId, projectId, true);

        mockMvc.perform(MockMvcRequestBuilders.get(TIME_ENTRIES_PATH + "/" + timeEntryId))
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
        long userId = createUser(mockMvc);
        long projectId = createProject(mockMvc, userId);
        long timeEntryId = createTimeEntry(mockMvc, userId, projectId, true);

        mockMvc.perform(MockMvcRequestBuilders.patch(TIME_ENTRIES_PATH + "/" + timeEntryId)
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
        long userId = createUser(mockMvc);
        long projectId = createProject(mockMvc, userId);
        long timeEntryId = createTimeEntry(mockMvc, userId, projectId, true);

        final String json = generatePatchTimeEntryJson();

        mockMvc.perform(MockMvcRequestBuilders.patch(TIME_ENTRIES_PATH + "/" + timeEntryId)
                .content(json)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.jsonPath("$.description").value("NewDescription"));
    }

    @Test
    public void testThatPatchTimeEntryChangesIsBillable() throws Exception {
        long userId = createUser(mockMvc);
        long projectId = createProject(mockMvc, userId);
        long timeEntryId = createTimeEntry(mockMvc, userId, projectId, true);

        final String json = """
                {
                    "projectId": %d,
                    "taskId": null,
                    "isBillable": false
                }
                    """.formatted(projectId);
        mockMvc.perform(MockMvcRequestBuilders.patch(TIME_ENTRIES_PATH + "/" + timeEntryId)
                .content(json)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.jsonPath("$.isBillable").value(false));
    }

    @Test
    public void testThatPatchTimeEntryWithGroupId() throws Exception {
        long userId = createUser(mockMvc);
        long projectId = createProject(mockMvc, userId);
        long timeEntryId = createTimeEntry(mockMvc, userId, projectId, true);
        long groupId = createGroup(mockMvc);

        final String json = """
                {
                    "groupId": %d,
                    "projectId": %d,
                    "taskId": null,
                    "isBillable": true
                }
                    """.formatted(groupId, projectId);
        mockMvc.perform(MockMvcRequestBuilders.patch(TIME_ENTRIES_PATH + "/" + timeEntryId)
                .content(json)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.jsonPath("$.groupId").value(groupId));
    }

    @Test
    public void testThatPatchTimeEntryWithNullProjectIdReturnsHttp400BadRequest() throws Exception {
        long userId = createUser(mockMvc);
        long projectId = createProject(mockMvc, userId);
        long timeEntryId = createTimeEntry(mockMvc, userId, projectId, true);

        final String json = """
                {
                    "projectId": null,
                    "taskId": null,
                    "isBillable": true
                }
                    """;
        mockMvc.perform(MockMvcRequestBuilders.patch(TIME_ENTRIES_PATH + "/" + timeEntryId)
                .content(json)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isBadRequest());
    }

    @Test
    public void testThatPatchTimeEntryWithNullIsBillableReturnsHttp400BadRequest() throws Exception {
        long userId = createUser(mockMvc);
        long projectId = createProject(mockMvc, userId);
        long timeEntryId = createTimeEntry(mockMvc, userId, projectId, true);

        final String json = """
                {
                    "projectId": %d,
                    "taskId": null,
                    "isBillable": null
                }
                    """.formatted(projectId);
        mockMvc.perform(MockMvcRequestBuilders.patch(TIME_ENTRIES_PATH + "/" + timeEntryId)
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
        long userId = createUser(mockMvc);
        long projectId = createProject(mockMvc, userId);
        long timeEntryId = createTimeEntry(mockMvc, userId, projectId, true);

        mockMvc.perform(MockMvcRequestBuilders.delete(TIME_ENTRIES_PATH + "/" + timeEntryId));

        mockMvc.perform(MockMvcRequestBuilders.get(TIME_ENTRIES_PATH))
                .andExpect(MockMvcResultMatchers.jsonPath("$").isEmpty());
    }

    @Test
    public void testThatDeleteTimeEntryReturnsHttp204NoContent() throws Exception {
        long userId = createUser(mockMvc);
        long projectId = createProject(mockMvc, userId);
        long timeEntryId = createTimeEntry(mockMvc, userId, projectId, true);

        mockMvc.perform(MockMvcRequestBuilders.delete(TIME_ENTRIES_PATH + "/" + timeEntryId))
                .andExpect(MockMvcResultMatchers.status().isNoContent());
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
