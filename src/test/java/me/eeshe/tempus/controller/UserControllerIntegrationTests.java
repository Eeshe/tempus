package me.eeshe.tempus.controller;

import static me.eeshe.tempus.testutil.TestEntityFactory.CLIENTS_PATH;
import static me.eeshe.tempus.testutil.TestEntityFactory.GROUPS_PATH;
import static me.eeshe.tempus.testutil.TestEntityFactory.PROJECTS_PATH;
import static me.eeshe.tempus.testutil.TestEntityFactory.TASKS_PATH;
import static me.eeshe.tempus.testutil.TestEntityFactory.TIME_ENTRIES_PATH;
import static me.eeshe.tempus.testutil.TestEntityFactory.USERS_PATH;
import static me.eeshe.tempus.testutil.TestEntityFactory.createClient;
import static me.eeshe.tempus.testutil.TestEntityFactory.createGroup;
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
public class UserControllerIntegrationTests extends BaseControllerTest {
    private final MockMvc mockMvc;

    @Autowired
    public UserControllerIntegrationTests(final MockMvc mockMvc) {
        this.mockMvc = mockMvc;
    }

    @Test
    public void testThatCreateUserReturnsValidUser() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.post(USERS_PATH)
                .content("""
                        {
                            "name": "MyUser"
                        }
                        """)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.jsonPath("$.id").isNumber())
                .andExpect(MockMvcResultMatchers.jsonPath("$.name").isNotEmpty())
                .andExpect(MockMvcResultMatchers.jsonPath("$.groupIds").isEmpty())
                .andExpect(MockMvcResultMatchers.jsonPath("$.createdAt").isNotEmpty());
    }

    @Test
    public void testThatCreateUserReturnsHttp201Created() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.post(USERS_PATH)
                .content("""
                        {
                            "name": "MyUser"
                        }
                        """)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isCreated());
    }

    @Test
    public void testThatCreateUserWithEmptyNameReturnsHttp400BadRequest() throws Exception {
        final String json = """
                {
                    "name": ""
                }
                    """;
        mockMvc.perform(MockMvcRequestBuilders.post(USERS_PATH)
                .content(json)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isBadRequest());
    }

    @Test
    public void testThatCreateUserWithNullNameReturnsHttp400BadRequest() throws Exception {
        final String json = "{}";

        mockMvc.perform(MockMvcRequestBuilders.post(USERS_PATH)
                .content(json)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isBadRequest());
    }

    @Test
    public void testThatListUsersReturnsNotEmptyList() throws Exception {
        createUser(mockMvc);

        mockMvc.perform(MockMvcRequestBuilders.get(USERS_PATH))
                .andExpect(MockMvcResultMatchers.jsonPath("$").isNotEmpty());
    }

    @Test
    public void testThatListUsersReturnsEmptyList() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get(USERS_PATH))
                .andExpect(MockMvcResultMatchers.jsonPath("$").isEmpty());
    }

    @Test
    public void testThatGetUserReturnsValidUser() throws Exception {
        final long userId = createUser(mockMvc);

        mockMvc.perform(MockMvcRequestBuilders.get(USERS_PATH + "/" + userId))
                .andExpect(MockMvcResultMatchers.jsonPath("$.id").isNumber())
                .andExpect(MockMvcResultMatchers.jsonPath("$.name").isNotEmpty())
                .andExpect(MockMvcResultMatchers.jsonPath("$.groupIds").isEmpty())
                .andExpect(MockMvcResultMatchers.jsonPath("$.createdAt").isNotEmpty());
    }

    @Test
    public void testThatGetUnexistentUserReturnsHttp400BadRequest() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get(USERS_PATH + "/9999"))
                .andExpect(MockMvcResultMatchers.status().isBadRequest());
    }

    @Test
    public void testThatUpdateUserReturnsValidUser() throws Exception {
        final long userId = createUser(mockMvc);

        mockMvc.perform(MockMvcRequestBuilders.put(USERS_PATH + "/" + userId)
                .content(generateUpdateUserJson())
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.jsonPath("$.id").isNumber())
                .andExpect(MockMvcResultMatchers.jsonPath("$.name").isNotEmpty())
                .andExpect(MockMvcResultMatchers.jsonPath("$.groupIds").isEmpty())
                .andExpect(MockMvcResultMatchers.jsonPath("$.createdAt").isNotEmpty());
    }

    @Test
    public void testThatUpdateUserChangesUserName() throws Exception {
        final long userId = createUser(mockMvc); // Creates user with name MyUser
        final String json = generateUpdateUserJson(); // JSON contains name change to MyNewUserName

        mockMvc.perform(MockMvcRequestBuilders.put(USERS_PATH + "/" + userId)
                .content(json)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.jsonPath("$.name").value("MyNewUserName"));
    }

    @Test
    public void testThatUpdateUserWithEmptyNameReturnsHttp400BadRequest() throws Exception {
        final String json = """
                {
                    "name": "",
                    "groupIds": []
                }
                    """;

        mockMvc.perform(MockMvcRequestBuilders.put(USERS_PATH + "/1")
                .content(json)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isBadRequest());
    }

    @Test
    public void testThatUpdateUserWithNullNameReturnsHttp400BadRequest() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.put(USERS_PATH + "/1")
                .content("{\"groupIds\": []}")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isBadRequest());
    }

    @Test
    public void testThatUpdateUserWithNullGroupIdsReturnsHttp400BadRequest() throws Exception {
        final String json = """
                {
                    "name": "MyUser"
                }
                    """;

        mockMvc.perform(MockMvcRequestBuilders.put(USERS_PATH + "/1")
                .content(json)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isBadRequest());
    }

    @Test
    public void testThatUpdateUserWithUnexistentGroupIdReturnsHttp400BadRequest() throws Exception {
        final long userId = createUser(mockMvc);

        final String json = """
                {
                    "name": "MyUser",
                    "groupIds": [1]
                }
                    """;
        mockMvc.perform(MockMvcRequestBuilders.put(USERS_PATH + "/" + userId)
                .content(json)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isBadRequest());
    }

    @Test
    public void testThatUpdateUnexistentUserReturnsHttp400BadRequest() throws Exception {
        final String json = generateUpdateUserJson();

        mockMvc.perform(MockMvcRequestBuilders.put(USERS_PATH + "/1")
                .content(json)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isBadRequest());
    }

    @Test
    public void testThatPatchUserReturnsValidUser() throws Exception {
        final long userId = createUser(mockMvc);

        mockMvc.perform(MockMvcRequestBuilders.patch(USERS_PATH + "/" + userId)
                .content(generatePatchUserJson())
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.jsonPath("$.id").isNumber())
                .andExpect(MockMvcResultMatchers.jsonPath("$.name").isNotEmpty())
                .andExpect(MockMvcResultMatchers.jsonPath("$.groupIds").isEmpty())
                .andExpect(MockMvcResultMatchers.jsonPath("$.createdAt").isNotEmpty());
    }

    @Test
    public void testThatPatchUserChangesUserName() throws Exception {
        final long userId = createUser(mockMvc); // Creates user with name MyUser
        final String json = generatePatchUserJson(); // JSON contains name change to MyNewUserName

        mockMvc.perform(MockMvcRequestBuilders.patch(USERS_PATH + "/" + userId)
                .content(json)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.jsonPath("$.name").value("MyNewUserName"));
    }

    @Test
    public void testThatPatchUserWithGroupIds() throws Exception {
        final long userId = createUser(mockMvc);

        final String json = """
                {
                    "groupIds": []
                }
                    """;
        mockMvc.perform(MockMvcRequestBuilders.patch(USERS_PATH + "/" + userId)
                .content(json)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.jsonPath("$.groupIds").isEmpty());
    }

    @Test
    public void testThatPatchUserWithEmptyNameReturnsHttp400BadRequest() throws Exception {
        final long userId = createUser(mockMvc);
        final String json = """
                {
                    "name": ""
                }
                    """;

        mockMvc.perform(MockMvcRequestBuilders.patch(USERS_PATH + "/" + userId)
                .content(json)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isBadRequest());
    }

    @Test
    public void testThatPatchUnexistentUserReturnsHttp400BadRequest() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.patch(USERS_PATH + "/1")
                .content(generatePatchUserJson())
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isBadRequest());
    }

    @Test
    public void testThatDeleteUserReturnsEmptyList() throws Exception {
        final long userId = createUser(mockMvc);

        mockMvc.perform(MockMvcRequestBuilders.delete(USERS_PATH + "/" + userId));

        mockMvc.perform(MockMvcRequestBuilders.get(USERS_PATH))
                .andExpect(MockMvcResultMatchers.jsonPath("$").isEmpty());
    }

    @Test
    public void testThatDeleteUserReturnsHttp204NoContent() throws Exception {
        final long userId = createUser(mockMvc);

        mockMvc.perform(MockMvcRequestBuilders.delete(USERS_PATH + "/" + userId))
                .andExpect(MockMvcResultMatchers.status().isNoContent());
    }

    @Test
    public void testThatDeleteUserCascadesIntoGroup() throws Exception {
        final long groupId = createGroup(mockMvc);
        mockMvc.perform(MockMvcRequestBuilders.get(GROUPS_PATH + "/" + groupId))
                .andExpect(MockMvcResultMatchers.jsonPath("$.userIds").isEmpty());

        final long userId = createUser(mockMvc);
        final String patchGroupJson = """
                {
                    "userIds": [%s]
                }
                    """.formatted(userId);
        mockMvc.perform(MockMvcRequestBuilders.patch(GROUPS_PATH + "/" + groupId)
                .content(patchGroupJson)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.jsonPath("$.userIds").isNotEmpty());

        mockMvc.perform(MockMvcRequestBuilders.delete(USERS_PATH + "/" + userId));
        mockMvc.perform(MockMvcRequestBuilders.get(GROUPS_PATH + "/" + groupId))
                .andExpect(MockMvcResultMatchers.jsonPath("$.userIds").isEmpty());
    }

    @Test
    public void testThatDeleteUserCascadesIntoClient() throws Exception {
        final long userId = createUser(mockMvc);
        final long clientId = createClient(mockMvc, userId);

        mockMvc.perform(MockMvcRequestBuilders.get(CLIENTS_PATH + "/" + clientId))
                .andExpect(MockMvcResultMatchers.jsonPath("$.userId").value(userId));

        mockMvc.perform(MockMvcRequestBuilders.delete(USERS_PATH + "/" + userId));
        mockMvc.perform(MockMvcRequestBuilders.get(CLIENTS_PATH + "/" + clientId))
                .andExpect(MockMvcResultMatchers.status().isBadRequest());
    }

    @Test
    public void testThatDeleteUserCascadesIntoProject() throws Exception {
        final long userId = createUser(mockMvc);
        final long projectId = createProject(mockMvc, userId);

        mockMvc.perform(MockMvcRequestBuilders.get(PROJECTS_PATH + "/" + projectId))
                .andExpect(MockMvcResultMatchers.jsonPath("$.userId").value(userId));

        mockMvc.perform(MockMvcRequestBuilders.delete(USERS_PATH + "/" + userId));
        mockMvc.perform(MockMvcRequestBuilders.get(PROJECTS_PATH + "/" + projectId))
                .andExpect(MockMvcResultMatchers.status().isBadRequest());
    }

    @Test
    public void testThatDeleteUserCascadesIntoTask() throws Exception {
        final long userId = createUser(mockMvc);
        final long projectId = createProject(mockMvc, userId);
        final long taskId = createTask(mockMvc, userId, projectId);

        mockMvc.perform(MockMvcRequestBuilders.get(TASKS_PATH + "/" + taskId))
                .andExpect(MockMvcResultMatchers.jsonPath("$.userId").value(userId));

        mockMvc.perform(MockMvcRequestBuilders.delete(USERS_PATH + "/" + userId));
        mockMvc.perform(MockMvcRequestBuilders.get(TASKS_PATH + "/" + taskId))
                .andExpect(MockMvcResultMatchers.status().isBadRequest());
    }

    @Test
    public void testThatDeleteUserCascadesIntoTimeEntries() throws Exception {
        final long userId = createUser(mockMvc);
        final long projectId = createProject(mockMvc, userId);
        final long timeEntryId = createTimeEntry(mockMvc, userId, projectId, false);

        mockMvc.perform(MockMvcRequestBuilders.get(TIME_ENTRIES_PATH + "/" + timeEntryId))
                .andExpect(MockMvcResultMatchers.jsonPath("$.userId").value(userId));

        mockMvc.perform(MockMvcRequestBuilders.delete(USERS_PATH + "/" + userId));
        mockMvc.perform(MockMvcRequestBuilders.get(TIME_ENTRIES_PATH + "/" + timeEntryId))
                .andExpect(MockMvcResultMatchers.status().isBadRequest());
    }

    private String generateUpdateUserJson() {
        return """
                {
                    "name": "MyNewUserName",
                    "groupIds": []
                }
                    """;
    }

    private String generatePatchUserJson() {
        return """
                {
                    "name": "MyNewUserName"
                }
                    """;
    }
}
