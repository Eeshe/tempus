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
public class GroupControllerIntegrationTests {
    private static final String GROUPS_PATH = "/api/v1/groups";
    private final MockMvc mockMvc;

    @Autowired
    public GroupControllerIntegrationTests(MockMvc mockMvc) {
        this.mockMvc = mockMvc;
    }

    @Test
    public void testThatCreateGroupReturnsValidGroup() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.post(GROUPS_PATH)
                .content(generateCreateGroupJson())
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.jsonPath("$.id").isNumber())
                .andExpect(MockMvcResultMatchers.jsonPath("$.name").isNotEmpty())
                .andExpect(MockMvcResultMatchers.jsonPath("$.userIds").isEmpty());
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
        createMockGroup();

        mockMvc.perform(MockMvcRequestBuilders.get(GROUPS_PATH))
                .andExpect(MockMvcResultMatchers.jsonPath("$").isNotEmpty());
    }

    @Test
    public void testThatUpdateGroupReturnsValidGroup() throws Exception {
        createMockGroup();

        mockMvc.perform(MockMvcRequestBuilders.put(GROUPS_PATH + "/1")
                .content(generateUpdateGroupJson())
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.jsonPath("$.id").isNumber())
                .andExpect(MockMvcResultMatchers.jsonPath("$.name").isNotEmpty())
                .andExpect(MockMvcResultMatchers.jsonPath("$.userIds").isEmpty());
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
    public void testThatUpdateUnexistentGroupReturnsHttp400BadRequest() throws Exception {
        final String json = generateUpdateGroupJson();

        mockMvc.perform(MockMvcRequestBuilders.put(GROUPS_PATH + "/1")
                .content(json)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isBadRequest());
    }

    @Test
    public void testThatDeleteGroupReturnsEmptyList() throws Exception {
        createMockGroup();

        mockMvc.perform(MockMvcRequestBuilders.delete(GROUPS_PATH + "/1"));

        mockMvc.perform(MockMvcRequestBuilders.get(GROUPS_PATH))
                .andExpect(MockMvcResultMatchers.jsonPath("$").isEmpty());
    }

    private void createMockGroup() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.post(GROUPS_PATH)
                .content(generateCreateGroupJson())
                .contentType(MediaType.APPLICATION_JSON));
    }

    private String generateCreateGroupJson() {
        return """
                {
                    "name": "MyGroup"
                }
                    """;
    }

    private String generateUpdateGroupJson() {
        return """
                {
                    "name": "MyOtherGroup",
                    "userIds": []
                }
                    """;
    }
}
