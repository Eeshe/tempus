package me.eeshe.tempus.testutil;

import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import com.jayway.jsonpath.JsonPath;

public class TestEntityFactory {
    public static final String USERS_PATH = "/api/v1/users";
    public static final String PROJECTS_PATH = "/api/v1/projects";
    public static final String CLIENTS_PATH = "/api/v1/clients";
    public static final String TASKS_PATH = "/api/v1/tasks";
    public static final String TIME_ENTRIES_PATH = "/api/v1/time-entries";

    public static long createUser(MockMvc mockMvc) throws Exception {
        return createUser(mockMvc, "MockUser");
    }

    public static long createUser(MockMvc mockMvc, String name) throws Exception {
        return postForId(mockMvc, USERS_PATH, """
                {
                    "name": "%s"
                }
                """.formatted(name));
    }

    public static long createProject(MockMvc mockMvc, long userId) throws Exception {
        return postForId(mockMvc, PROJECTS_PATH, """
                {
                    "name": "MyProject",
                    "userId": %d
                }
                """.formatted(userId));
    }

    public static long createClient(MockMvc mockMvc, long userId) throws Exception {
        return createClient(mockMvc, userId, "MyClient", 25.0);
    }

    public static long createClient(MockMvc mockMvc, long userId, String name, double hourlyRate) throws Exception {
        return postForId(mockMvc, CLIENTS_PATH, """
                {
                    "name": "%s",
                    "userId": %d,
                    "hourlyRate": %.1f
                }
                """.formatted(name, userId, hourlyRate));
    }

    public static long createTask(MockMvc mockMvc, long userId, long projectId) throws Exception {
        return createTask(mockMvc, userId, projectId, "MyTask");
    }

    public static long createTask(MockMvc mockMvc, long userId, long projectId, String name) throws Exception {
        return postForId(mockMvc, TASKS_PATH, """
                {
                    "name": "%s",
                    "userId": %d,
                    "projectId": %d
                }
                """.formatted(name, userId, projectId));
    }

    public static long createTimeEntry(MockMvc mockMvc, long userId, long projectId, boolean isBillable)
            throws Exception {
        return postForId(mockMvc, TIME_ENTRIES_PATH, """
                {
                    "userId": %d,
                    "projectId": %d,
                    "isBillable": %s
                }
                """.formatted(userId, projectId, isBillable));
    }

    private static long postForId(MockMvc mockMvc, String path, String json) throws Exception {
        MvcResult result = mockMvc.perform(MockMvcRequestBuilders.post(path)
                .content(json)
                .contentType(MediaType.APPLICATION_JSON))
                .andReturn();

        return JsonPath.parse(result.getResponse().getContentAsString()).read("$.id", Long.class);
    }
}
