package me.eeshe.tempus.controller;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
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
    public void testThatCreateGroupReturnsHttp201Created() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.post(GROUPS_PATH)
                .header("name", "MyGroup"))
                .andExpect(MockMvcResultMatchers.status().isCreated());
    }

    @Test
    public void testThatCreateGroupReturnsValidGroup() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.post(GROUPS_PATH)
                .header("name", "MyGroup"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.id").isNumber())
                .andExpect(MockMvcResultMatchers.jsonPath("$.name").isNotEmpty());
    }

    @Test
    public void testThatListGroupsReturnsNotEmptyList() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.post(GROUPS_PATH)
                .header("name", "MyGroup"));

        mockMvc.perform(MockMvcRequestBuilders.get(GROUPS_PATH))
                .andExpect(MockMvcResultMatchers.jsonPath("$").isNotEmpty());
    }
}
