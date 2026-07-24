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
public class ClientControllerIntegrationTests {
    private final MockMvc mockMvc;

    @Autowired
    public ClientControllerIntegrationTests(MockMvc mockMvc) {
        this.mockMvc = mockMvc;
    }

    @Test
    public void testThatCreateClientReturnsValidClient() throws Exception {
        createUser(mockMvc);

        mockMvc.perform(MockMvcRequestBuilders.post(CLIENTS_PATH)
                .content("""
                        {
                            "name": "MyClient",
                            "userId": 1,
                            "hourlyRate": 25.0
                        }
                        """)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.jsonPath("$.id").isNumber())
                .andExpect(MockMvcResultMatchers.jsonPath("$.name").isNotEmpty())
                .andExpect(MockMvcResultMatchers.jsonPath("$.userId").isNumber())
                .andExpect(MockMvcResultMatchers.jsonPath("$.hourlyRate").isNumber())
                .andExpect(MockMvcResultMatchers.jsonPath("$.createdAt").isNotEmpty());
    }

    @Test
    public void testThatCreateClientReturnsHttp201Created() throws Exception {
        createUser(mockMvc);

        mockMvc.perform(MockMvcRequestBuilders.post(CLIENTS_PATH)
                .content("""
                        {
                            "name": "MyClient",
                            "userId": 1,
                            "hourlyRate": 25.0
                        }
                        """)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isCreated());
    }

    @Test
    public void testThatCreateClientWithEmptyNameReturnsHttp400BadRequest() throws Exception {
        final String json = """
                {
                    "name": "",
                    "userId": 1
                }
                    """;
        mockMvc.perform(MockMvcRequestBuilders.post(CLIENTS_PATH)
                .content(json)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isBadRequest());
    }

    @Test
    public void testThatCreateClientWithNullNameReturnsHttp400BadRequest() throws Exception {
        final String json = """
                {
                    "userId": 1
                }
                    """;
        mockMvc.perform(MockMvcRequestBuilders.post(CLIENTS_PATH)
                .content(json)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isBadRequest());
    }

    @Test
    public void testThatCreateClientWithNullUserIdReturnsHttp400BadRequest() throws Exception {
        final String json = """
                {
                    "name": "MyClient"
                }
                    """;
        mockMvc.perform(MockMvcRequestBuilders.post(CLIENTS_PATH)
                .content(json)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isBadRequest());
    }

    @Test
    public void testThatCreateClientWithUnexistentUserIdReturnsHttp400BadRequest() throws Exception {
        final String json = """
                {
                    "name": "MyClient",
                    "userId": 9999
                }
                    """;
        mockMvc.perform(MockMvcRequestBuilders.post(CLIENTS_PATH)
                .content(json)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isBadRequest());
    }

    @Test
    public void testThatCreateClientWithNegativeHourlyRateReturnsHttp400BadRequest() throws Exception {
        final String json = """
                {
                    "name": "MyClient",
                    "userId": 1,
                    "hourlyRate": -1
                }
                    """;
        mockMvc.perform(MockMvcRequestBuilders.post(CLIENTS_PATH)
                .content(json)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isBadRequest());
    }

    @Test
    public void testThatListClientsReturnsNotEmptyList() throws Exception {
        createUser(mockMvc);
        createClient(mockMvc, 1);

        mockMvc.perform(MockMvcRequestBuilders.get(CLIENTS_PATH))
                .andExpect(MockMvcResultMatchers.jsonPath("$").isNotEmpty());
    }

    @Test
    public void testThatListClientsReturnsEmptyList() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get(CLIENTS_PATH))
                .andExpect(MockMvcResultMatchers.jsonPath("$").isEmpty());
    }

    @Test
    public void testThatGetClientReturnsValidClient() throws Exception {
        long userId = createUser(mockMvc);
        long clientId = createClient(mockMvc, userId);

        mockMvc.perform(MockMvcRequestBuilders.get(CLIENTS_PATH + "/" + clientId))
                .andExpect(MockMvcResultMatchers.jsonPath("$.id").isNumber())
                .andExpect(MockMvcResultMatchers.jsonPath("$.name").isNotEmpty())
                .andExpect(MockMvcResultMatchers.jsonPath("$.userId").isNumber())
                .andExpect(MockMvcResultMatchers.jsonPath("$.hourlyRate").isNumber())
                .andExpect(MockMvcResultMatchers.jsonPath("$.createdAt").isNotEmpty());
    }

    @Test
    public void testThatGetUnexistentClientReturnsHttp400BadRequest() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get(CLIENTS_PATH + "/9999"))
                .andExpect(MockMvcResultMatchers.status().isBadRequest());
    }

    @Test
    public void testThatPatchClientReturnsValidClient() throws Exception {
        long userId = createUser(mockMvc);
        long clientId = createClient(mockMvc, userId);

        mockMvc.perform(MockMvcRequestBuilders.patch(CLIENTS_PATH + "/" + clientId)
                .content(generatePatchClientJson())
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.jsonPath("$.id").isNumber())
                .andExpect(MockMvcResultMatchers.jsonPath("$.name").isNotEmpty())
                .andExpect(MockMvcResultMatchers.jsonPath("$.userId").isNumber())
                .andExpect(MockMvcResultMatchers.jsonPath("$.hourlyRate").isNumber())
                .andExpect(MockMvcResultMatchers.jsonPath("$.createdAt").isNotEmpty());
    }

    @Test
    public void testThatPatchClientChangesClientName() throws Exception {
        long userId = createUser(mockMvc); // Creates user with name MockUser
        long clientId = createClient(mockMvc, userId); // Creates client with name MyClient
        final String json = generatePatchClientJson(); // JSON contains name change to MyNewClientName

        mockMvc.perform(MockMvcRequestBuilders.patch(CLIENTS_PATH + "/" + clientId)
                .content(json)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.jsonPath("$.name").value("MyNewClientName"));
    }

    @Test
    public void testThatPatchClientWithEmptyNameReturnsHttp400BadRequest() throws Exception {
        long userId = createUser(mockMvc);
        long clientId = createClient(mockMvc, userId);
        final String json = """
                {
                    "name": ""
                }
                    """;

        mockMvc.perform(MockMvcRequestBuilders.patch(CLIENTS_PATH + "/" + clientId)
                .content(json)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isBadRequest());
    }

    @Test
    public void testThatPatchClientWithNegativeHourlyRateReturnsHttp400BadRequest() throws Exception {
        long userId = createUser(mockMvc);
        long clientId = createClient(mockMvc, userId);
        final String json = """
                {
                    "hourlyRate": -1
                }
                    """;

        mockMvc.perform(MockMvcRequestBuilders.patch(CLIENTS_PATH + "/" + clientId)
                .content(json)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isBadRequest());
    }

    @Test
    public void testThatPatchClientWithHourlyRate() throws Exception {
        long userId = createUser(mockMvc); // Creates user with name MockUser
        long clientId = createClient(mockMvc, userId); // Creates client with hourlyRate 25.0

        final String json = """
                {
                    "hourlyRate": 50.0
                }
                    """;
        mockMvc.perform(MockMvcRequestBuilders.patch(CLIENTS_PATH + "/" + clientId)
                .content(json)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.jsonPath("$.hourlyRate").value(50.0));
    }

    @Test
    public void testThatPatchUnexistentClientReturnsHttp400BadRequest() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.patch(CLIENTS_PATH + "/1")
                .content(generatePatchClientJson())
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isBadRequest());
    }

    @Test
    public void testThatDeleteClientReturnsEmptyList() throws Exception {
        long userId = createUser(mockMvc);
        long clientId = createClient(mockMvc, userId);

        mockMvc.perform(MockMvcRequestBuilders.delete(CLIENTS_PATH + "/" + clientId));

        mockMvc.perform(MockMvcRequestBuilders.get(CLIENTS_PATH))
                .andExpect(MockMvcResultMatchers.jsonPath("$").isEmpty());
    }

    @Test
    public void testThatDeleteClientReturnsHttp204NoContent() throws Exception {
        long userId = createUser(mockMvc);
        long clientId = createClient(mockMvc, userId);

        mockMvc.perform(MockMvcRequestBuilders.delete(CLIENTS_PATH + "/" + clientId))
                .andExpect(MockMvcResultMatchers.status().isNoContent());
    }

    private String generatePatchClientJson() {
        return """
                {
                    "name": "MyNewClientName"
                }
                    """;
    }
}
