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
public class UserControllerIntegrationTests {
    private static final String USERS_PATH = "/api/v1/users";
    private final MockMvc mockMvc;

    @Autowired
    public UserControllerIntegrationTests(MockMvc mockMvc) {
        this.mockMvc = mockMvc;
    }

    @Test
    public void testThatCreateUserReturnsValidUser() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.post(USERS_PATH)
                .content(generateCreateUserJson())
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.jsonPath("$.id").isNumber())
                .andExpect(MockMvcResultMatchers.jsonPath("$.name").isNotEmpty())
                .andExpect(MockMvcResultMatchers.jsonPath("$.groupIds").isEmpty())
                .andExpect(MockMvcResultMatchers.jsonPath("$.createdAt").isNotEmpty());
    }

    @Test
    public void testThatCreateUserReturnsHttp201Created() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.post(USERS_PATH)
                .content(generateCreateUserJson())
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
        createMockUser();

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
        createMockUser();

        mockMvc.perform(MockMvcRequestBuilders.get(USERS_PATH + "/1"))
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
        createMockUser();

        mockMvc.perform(MockMvcRequestBuilders.put(USERS_PATH + "/1")
                .content(generateUpdateUserJson())
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.jsonPath("$.id").isNumber())
                .andExpect(MockMvcResultMatchers.jsonPath("$.name").isNotEmpty())
                .andExpect(MockMvcResultMatchers.jsonPath("$.groupIds").isEmpty())
                .andExpect(MockMvcResultMatchers.jsonPath("$.createdAt").isNotEmpty());
    }

    @Test
    public void testThatUpdateUserChangesUserName() throws Exception {
        createMockUser(); // Creates user with name MyUser
        final String json = generateUpdateUserJson(); // JSON contains name change to MyNewUserName

        mockMvc.perform(MockMvcRequestBuilders.put(USERS_PATH + "/1")
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
        createMockUser();

        final String json = """
                {
                    "name": "MyUser",
                    "groupIds": [1]
                }
                    """;
        mockMvc.perform(MockMvcRequestBuilders.put(USERS_PATH + "/1")
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
        createMockUser();

        mockMvc.perform(MockMvcRequestBuilders.patch(USERS_PATH + "/1")
                .content(generatePatchUserJson())
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.jsonPath("$.id").isNumber())
                .andExpect(MockMvcResultMatchers.jsonPath("$.name").isNotEmpty())
                .andExpect(MockMvcResultMatchers.jsonPath("$.groupIds").isEmpty())
                .andExpect(MockMvcResultMatchers.jsonPath("$.createdAt").isNotEmpty());
    }

    @Test
    public void testThatPatchUserChangesUserName() throws Exception {
        createMockUser(); // Creates user with name MyUser
        final String json = generatePatchUserJson(); // JSON contains name change to MyNewUserName

        mockMvc.perform(MockMvcRequestBuilders.patch(USERS_PATH + "/1")
                .content(json)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.jsonPath("$.name").value("MyNewUserName"));
    }

    @Test
    public void testThatPatchUserWithGroupIds() throws Exception {
        createMockUser();

        final String json = """
                {
                    "groupIds": []
                }
                    """;
        mockMvc.perform(MockMvcRequestBuilders.patch(USERS_PATH + "/1")
                .content(json)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.jsonPath("$.groupIds").isEmpty());
    }

    @Test
    public void testThatPatchUserWithEmptyNameReturnsHttp400BadRequest() throws Exception {
        createMockUser();
        final String json = """
                {
                    "name": ""
                }
                    """;

        mockMvc.perform(MockMvcRequestBuilders.patch(USERS_PATH + "/1")
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
        createMockUser();

        mockMvc.perform(MockMvcRequestBuilders.delete(USERS_PATH + "/1"));

        mockMvc.perform(MockMvcRequestBuilders.get(USERS_PATH))
                .andExpect(MockMvcResultMatchers.jsonPath("$").isEmpty());
    }

    @Test
    public void testThatDeleteUserReturnsHttp204NoContent() throws Exception {
        createMockUser();

        mockMvc.perform(MockMvcRequestBuilders.delete(USERS_PATH + "/1"))
                .andExpect(MockMvcResultMatchers.status().isNoContent());
    }

    private void createMockUser() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.post(USERS_PATH)
                .content(generateCreateUserJson())
                .contentType(MediaType.APPLICATION_JSON));
    }

    private String generateCreateUserJson() {
        return """
                {
                    "name": "MyUser"
                }
                    """;
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
