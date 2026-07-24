package me.eeshe.tempus.controller;

import static me.eeshe.tempus.testutil.TestEntityFactory.GROUPS_PATH;
import static me.eeshe.tempus.testutil.TestEntityFactory.USERS_PATH;
import static me.eeshe.tempus.testutil.TestEntityFactory.createGroup;
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
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@ExtendWith(SpringExtension.class)
@AutoConfigureMockMvc
@Transactional
public class GroupControllerIntegrationTests {
    private final MockMvc mockMvc;

    @Autowired
    public GroupControllerIntegrationTests(final MockMvc mockMvc) {
        this.mockMvc = mockMvc;
    }

    @Test
    public void testThatCreateGroupReturnsValidGroup() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.post(GROUPS_PATH)
                .content("""
                        {
                            "name": "MyGroup"
                        }
                        """)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.jsonPath("$.id").isNumber())
                .andExpect(MockMvcResultMatchers.jsonPath("$.name").isNotEmpty())
                .andExpect(MockMvcResultMatchers.jsonPath("$.userIds").isEmpty())
                .andExpect(MockMvcResultMatchers.jsonPath("$.createdAt").isNotEmpty());
    }

    @Test
    public void testThatCreateGroupReturnsHttp201Created() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.post(GROUPS_PATH)
                .content("""
                        {
                            "name": "MyGroup"
                        }
                        """)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isCreated());
    }

    @Test
    public void testThatCreateGroupWithEmptyNameReturnsHttp400BadRequest() throws Exception {
        final String json = """
                {
                    "name": ""
                }
                    """;
        mockMvc.perform(MockMvcRequestBuilders.post(GROUPS_PATH)
                .content(json)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isBadRequest());
    }

    @Test
    public void testThatCreateGroupWithNullNameReturnsHttp400BadRequest() throws Exception {
        final String json = "{}";

        mockMvc.perform(MockMvcRequestBuilders.post(GROUPS_PATH)
                .content(json)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isBadRequest());
    }

    @Test
    public void testThatListGroupsReturnsNotEmptyList() throws Exception {
        createGroup(mockMvc);

        mockMvc.perform(MockMvcRequestBuilders.get(GROUPS_PATH))
                .andExpect(MockMvcResultMatchers.jsonPath("$").isNotEmpty());
    }

    @Test
    public void testThatListGroupsReturnsEmptyList() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get(GROUPS_PATH))
                .andExpect(MockMvcResultMatchers.jsonPath("$").isEmpty());
    }

    @Test
    public void testThatGetGroupReturnsValidGroup() throws Exception {
        final long groupId = createGroup(mockMvc);

        mockMvc.perform(MockMvcRequestBuilders.get(GROUPS_PATH + "/" + groupId))
                .andExpect(MockMvcResultMatchers.jsonPath("$.id").isNumber())
                .andExpect(MockMvcResultMatchers.jsonPath("$.name").isNotEmpty())
                .andExpect(MockMvcResultMatchers.jsonPath("$.userIds").isEmpty())
                .andExpect(MockMvcResultMatchers.jsonPath("$.createdAt").isNotEmpty());
    }

    @Test
    public void testThatGetUnexistentGroupReturnsHttp400BadRequest() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get(GROUPS_PATH + "/9999"))
                .andExpect(MockMvcResultMatchers.status().isBadRequest());
    }

    @Test
    public void testThatUpdateGroupReturnsValidGroup() throws Exception {
        final long groupId = createGroup(mockMvc);

        mockMvc.perform(MockMvcRequestBuilders.put(GROUPS_PATH + "/" + groupId)
                .content(generateUpdateGroupJson())
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.jsonPath("$.id").isNumber())
                .andExpect(MockMvcResultMatchers.jsonPath("$.name").isNotEmpty())
                .andExpect(MockMvcResultMatchers.jsonPath("$.userIds").isEmpty());
    }

    @Test
    public void testThatUpdateGroupChangesGroupName() throws Exception {
        final long groupId = createGroup(mockMvc); // Creates group with name MyGroup
        final String json = generateUpdateGroupJson(); // JSON contains name change to MyNewGroupName

        mockMvc.perform(MockMvcRequestBuilders.put(GROUPS_PATH + "/" + groupId)
                .content(json)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.jsonPath("$.name").value("MyNewGroupName"));
    }

    @Test
    public void testThatUpdateGroupWithEmptyNameReturnsHttp400BadRequest() throws Exception {
        final String json = """
                {
                    "name": ""
                }
                    """;

        mockMvc.perform(MockMvcRequestBuilders.put(GROUPS_PATH + "/1")
                .content(json)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isBadRequest());
    }

    @Test
    public void testThatUpdateGroupWithNullNameReturnsHttp400BadRequest() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.put(GROUPS_PATH + "/1")
                .content("{}")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isBadRequest());
    }

    @Test
    public void testThatUpdateGroupWithNullUserIdsReturnsHttp400BadRequest() throws Exception {
        final long groupId = createGroup(mockMvc);

        final String json = """
                {
                    "name": "MyGroup",
                }
                    """;
        mockMvc.perform(MockMvcRequestBuilders.put(GROUPS_PATH + "/" + groupId)
                .content(json)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isBadRequest());
    }

    @Test
    public void testThatUpdateGroupWithUnexistentUserIdReturnsHttp400BadRequest() throws Exception {
        final long groupId = createGroup(mockMvc);

        final String json = """
                {
                    "name": "MyGroup",
                    "userIds": [1]
                }
                    """;
        mockMvc.perform(MockMvcRequestBuilders.put(GROUPS_PATH + "/" + groupId)
                .content(json)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isBadRequest());
    }

    @Test
    public void testThatUpdateUnexistentGroupReturnsHttp400BadRequest() throws Exception {
        final String json = generateUpdateGroupJson();

        mockMvc.perform(MockMvcRequestBuilders.put(GROUPS_PATH + "/1")
                .content(json)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isBadRequest());
    }

    @Test
    public void testThatPatchGroupReturnsValidGroup() throws Exception {
        final long groupId = createGroup(mockMvc);

        mockMvc.perform(MockMvcRequestBuilders.patch(GROUPS_PATH + "/" + groupId)
                .content(generatePatchGroupJson())
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.jsonPath("$.id").isNumber())
                .andExpect(MockMvcResultMatchers.jsonPath("$.name").isNotEmpty())
                .andExpect(MockMvcResultMatchers.jsonPath("$.userIds").isEmpty());
    }

    @Test
    public void testThatPatchGroupChangesGroupName() throws Exception {
        final long groupId = createGroup(mockMvc); // Creates group with name MyNewGroup
        final String json = generatePatchGroupJson(); // JSON contains name change to MyNewGroupName

        mockMvc.perform(MockMvcRequestBuilders.patch(GROUPS_PATH + "/" + groupId)
                .content(json)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.jsonPath("$.name").value("MyNewGroupName"));
    }

    @Test
    public void testThatPatchGroupWithUserIds() throws Exception {
        final long groupId = createGroup(mockMvc);

        final String json = """
                {
                    "userIds": []
                }
                    """;
        mockMvc.perform(MockMvcRequestBuilders.patch(GROUPS_PATH + "/" + groupId)
                .content(json)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.jsonPath("$.userIds").isEmpty());
    }

    @Test
    public void testThatPatchGroupWithEmptyNameReturnsHttp400BadRequest() throws Exception {
        final long groupId = createGroup(mockMvc);
        final String json = """
                {
                    "name": ""
                }
                    """;

        mockMvc.perform(MockMvcRequestBuilders.patch(GROUPS_PATH + "/" + groupId)
                .content(json)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isBadRequest());
    }

    @Test
    public void testThatPatchUnexistentGroupReturnsHttp400BadRequest() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.patch(GROUPS_PATH + "/1")
                .content(generatePatchGroupJson())
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isBadRequest());
    }

    @Test
    public void testThatDeleteGroupReturnsEmptyList() throws Exception {
        final long groupId = createGroup(mockMvc);

        mockMvc.perform(MockMvcRequestBuilders.delete(GROUPS_PATH + "/" + groupId));

        mockMvc.perform(MockMvcRequestBuilders.get(GROUPS_PATH))
                .andExpect(MockMvcResultMatchers.jsonPath("$").isEmpty());
    }

    @Test
    public void testThatDeleteGroupReturnsHttp204NoContent() throws Exception {
        final long groupId = createGroup(mockMvc);

        mockMvc.perform(MockMvcRequestBuilders.delete(GROUPS_PATH + "/" + groupId))
                .andExpect(MockMvcResultMatchers.status().isNoContent());
    }

    @Test
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    public void testThatDeleteGroupCascadesIntoUser() throws Exception {
        final long userId = createUser(mockMvc);
        mockMvc.perform(MockMvcRequestBuilders.get(USERS_PATH + "/" + userId))
                .andExpect(MockMvcResultMatchers.jsonPath("$.groupIds").isEmpty());

        final long groupId = createGroup(mockMvc);
        final String patchUserJson = """
                {
                    "groupIds": [%s]
                }
                    """.formatted(groupId);

        mockMvc.perform(MockMvcRequestBuilders.patch(USERS_PATH + "/" + userId)
                .content(patchUserJson)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.jsonPath("$.groupIds").isNotEmpty());

        mockMvc.perform(MockMvcRequestBuilders.delete(GROUPS_PATH + "/" + groupId));
        mockMvc.perform(MockMvcRequestBuilders.get(USERS_PATH + "/" + groupId))
                .andExpect(MockMvcResultMatchers.jsonPath("$.groupIds").isEmpty());
    }

    private String generateUpdateGroupJson() {
        return """
                {
                    "name": "MyNewGroupName",
                    "userIds": []
                }
                    """;
    }

    private String generatePatchGroupJson() {
        return """
                {
                    "name": "MyNewGroupName"
                }
                    """;
    }
}
