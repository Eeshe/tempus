package me.eeshe.tempus;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.List;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.util.ReflectionTestUtils;

import me.eeshe.tempus.controller.ClientController;
import me.eeshe.tempus.dto.ClientDTO;
import me.eeshe.tempus.dto.CreateClientRequestDTO;
import me.eeshe.tempus.dto.PatchClientRequestDTO;
import me.eeshe.tempus.entity.Client;
import me.eeshe.tempus.exception.ClientNotFoundException;
import me.eeshe.tempus.exception.UserClientAlreadyExistsException;
import me.eeshe.tempus.mapper.ClientMapper;
import me.eeshe.tempus.request.CreateClientRequest;
import me.eeshe.tempus.request.PatchClientRequest;
import me.eeshe.tempus.service.ClientService;
import me.eeshe.tempus.support.ControllerTestBase;

@WebMvcTest(ClientController.class)
public class ClientControllerTest extends ControllerTestBase {
    private static final long USER_ID = 1L;
    private static final long CLIENT_ID = 10L;

    private static final Instant CREATED_AT = Instant.parse("2026-01-01T10:00:00Z");

    private static final String CLIENT_NAME_NULL_OR_EMPTY_ERROR_MESSAGE = "Client name can't be null or empty";

    private static final String CREATE_CLIENT_JSON_BODY = """
            {
                "name": "MyClient"
            }""";

    private static final String PATCH_CLIENT_JSON_BODY = """
            {
                "name": "MyClient"
            }""";

    @MockitoBean
    private ClientService clientService;

    @MockitoBean
    private ClientMapper clientMapper;

    @Nested
    class ListClients {
        private static final String URL = "/api/v1/clients";

        @Test
        void returnsAuthenticatedUserClients() {
            final Client client = createClient();

            when(clientService.listClients(USER_ID)).thenReturn(List.of(client));
            when(clientMapper.toDTO(client)).thenReturn(createClientDTO());

            assertThat(mockMvc.get().uri(URL).with(createPrincipal(USER_ID)))
                    .hasStatus(200)
                    .bodyJson()
                    .isEqualTo("[%s]".formatted(createClientDTOJson()));

            verify(clientService).listClients(USER_ID);
        }

        @Test
        void returnsAuthenticatedEmptyUserClients() {
            assertThat(mockMvc.get().uri(URL).with(createPrincipal(USER_ID)))
                    .hasStatus(200)
                    .bodyJson()
                    .isEqualTo("[]");

            verify(clientService).listClients(USER_ID);
        }

        @Test
        void rejectsUnauthenticatedRequest() {
            assertThat(mockMvc.get().uri(URL)).hasStatus(401);

            verifyNoInteractions(clientService);
        }
    }

    @Nested
    class GetClient {
        private static final String URL = "/api/v1/clients/{clientId}";

        @Test
        void returnsAuthenticatedUserClient() {
            final Client client = createClient();

            when(clientService.getClient(USER_ID, CLIENT_ID)).thenReturn(client);
            when(clientMapper.toDTO(client)).thenReturn(createClientDTO());

            assertThat(mockMvc.get().uri(URL, CLIENT_ID).with(createPrincipal(USER_ID)))
                    .hasStatus(200)
                    .bodyJson()
                    .isEqualTo(createClientDTOJson());

            verify(clientService).getClient(USER_ID, CLIENT_ID);
        }

        @Test
        void rejectsUnauthenticatedRequest() {
            assertThat(mockMvc.get().uri(URL, CLIENT_ID)).hasStatus(401);

            verifyNoInteractions(clientService);
        }

        @Test
        void rejectsNonExistentClient() {
            when(clientService.getClient(USER_ID, CLIENT_ID)).thenThrow(new ClientNotFoundException(CLIENT_ID));

            assertThat(mockMvc.get().uri(URL, CLIENT_ID).with(createPrincipal(USER_ID)))
                    .hasStatus(404)
                    .bodyJson()
                    .extractingPath("$.error").asString()
                    .isEqualTo(new ClientNotFoundException(CLIENT_ID).getMessage());

            verify(clientService).getClient(USER_ID, CLIENT_ID);
        }
    }

    @Nested
    class CreateClient {
        private static final String URL = "/api/v1/clients";

        @Test
        void returnsCreatedClient() {
            final Client createdClient = createClient();
            final CreateClientRequest createClientRequest = createClientRequest();
            final CreateClientRequestDTO createClientRequestDTO = createCreateClientRequestDTO();

            when(clientMapper.fromDTO(
                    eq(createClientRequestDTO),
                    eq(USER_ID)))
                    .thenReturn(createClientRequest);
            when(clientService.createClient(createClientRequest)).thenReturn(createdClient);
            when(clientMapper.toDTO(eq(createdClient))).thenReturn(createClientDTO());

            assertThat(mockMvc.post().uri(URL).with(createPrincipal(USER_ID))
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(CREATE_CLIENT_JSON_BODY))
                    .hasStatus(201)
                    .bodyJson()
                    .isEqualTo(createClientDTOJson());

            verify(clientMapper).fromDTO(
                    eq(createClientRequestDTO),
                    eq(USER_ID));
            verify(clientService).createClient(createClientRequest);
        }

        @Test
        void rejectsUnauthenticatedRequest() {
            assertThat(mockMvc.post().uri(URL)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(CREATE_CLIENT_JSON_BODY))
                    .hasStatus(401);

            verifyNoInteractions(clientMapper, clientService);
        }

        @Test
        void rejectsAlreadyExistentUserClient() {
            final CreateClientRequest createClientRequest = createClientRequest();
            final CreateClientRequestDTO createClientRequestDTO = createCreateClientRequestDTO();

            when(clientMapper.fromDTO(
                    eq(createClientRequestDTO),
                    eq(USER_ID)))
                    .thenReturn(createClientRequest);
            when(clientService.createClient(createClientRequest))
                    .thenThrow(new UserClientAlreadyExistsException(USER_ID, "MyClient"));

            assertThat(mockMvc.post().uri(URL).with(createPrincipal(USER_ID))
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(CREATE_CLIENT_JSON_BODY))
                    .hasStatus(400)
                    .bodyJson()
                    .extractingPath("$.error").asString()
                    .isEqualTo(new UserClientAlreadyExistsException(USER_ID, "MyClient").getMessage());

            verify(clientMapper).fromDTO(
                    eq(createClientRequestDTO),
                    eq(USER_ID));
            verify(clientService).createClient(createClientRequest);
        }

        @Test
        void rejectsEmptyClientName() {
            final String jsonBody = """
                    {
                        "name": ""
                    }""";

            assertThat(mockMvc.post().uri(URL).with(createPrincipal(USER_ID))
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(jsonBody))
                    .hasStatus(400)
                    .bodyJson()
                    .extractingPath("$.error").asString().isEqualTo(CLIENT_NAME_NULL_OR_EMPTY_ERROR_MESSAGE);

            verifyNoInteractions(clientMapper, clientService);
        }

        @Test
        void rejectsBlankClientName() {
            final String jsonBody = """
                    {
                        "name": "  "
                    }""";

            assertThat(mockMvc.post().uri(URL).with(createPrincipal(USER_ID))
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(jsonBody))
                    .hasStatus(400)
                    .bodyJson()
                    .extractingPath("$.error").asString().isEqualTo(CLIENT_NAME_NULL_OR_EMPTY_ERROR_MESSAGE);

            verifyNoInteractions(clientMapper, clientService);
        }

        @Test
        void rejectsNonProvidedClientName() {
            final String jsonBody = """
                    {
                    }""";

            assertThat(mockMvc.post().uri(URL).with(createPrincipal(USER_ID))
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(jsonBody))
                    .hasStatus(400)
                    .bodyJson()
                    .extractingPath("$.error").asString().isEqualTo(CLIENT_NAME_NULL_OR_EMPTY_ERROR_MESSAGE);

            verifyNoInteractions(clientMapper, clientService);
        }
    }

    @Nested
    class PatchClient {
        private static final String URL = "/api/v1/clients/{clientId}";

        @Test
        void patchClientName() {
            final Client patchedClient = createClient();
            final PatchClientRequest patchClientRequest = createPatchClientRequest();
            final PatchClientRequestDTO patchClientRequestDTO = createPatchClientRequestDTO();

            when(clientMapper.fromDTO(
                    eq(patchClientRequestDTO),
                    eq(USER_ID)))
                    .thenReturn(patchClientRequest);
            when(clientService.patchClient(
                    eq(USER_ID),
                    eq(CLIENT_ID),
                    eq(patchClientRequest))).thenReturn(patchedClient);
            when(clientMapper.toDTO(eq(patchedClient))).thenReturn(createClientDTO());

            assertThat(mockMvc.patch().uri(URL, CLIENT_ID).with(createPrincipal(USER_ID))
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(PATCH_CLIENT_JSON_BODY))
                    .hasStatus(200)
                    .bodyJson()
                    .isEqualTo(createClientDTOJson());

            verify(clientMapper).fromDTO(
                    eq(patchClientRequestDTO),
                    eq(USER_ID));
            verify(clientService).patchClient(
                    eq(USER_ID),
                    eq(CLIENT_ID),
                    eq(patchClientRequest));
        }

        @Test
        void rejectsUnauthenticatedRequest() {
            assertThat(mockMvc.patch().uri(URL, CLIENT_ID)).hasStatus(401);

            verifyNoInteractions(clientMapper, clientService);
        }

        @Test
        void rejectsNonExistentClient() {
            final PatchClientRequest patchClientRequest = createPatchClientRequest();
            final PatchClientRequestDTO patchClientRequestDTO = createPatchClientRequestDTO();

            when(clientMapper.fromDTO(
                    eq(patchClientRequestDTO),
                    eq(USER_ID)))
                    .thenReturn(patchClientRequest);
            when(clientService.patchClient(
                    eq(USER_ID),
                    eq(CLIENT_ID),
                    eq(patchClientRequest))).thenThrow(new ClientNotFoundException(CLIENT_ID));

            assertThat(mockMvc.patch().uri(URL, CLIENT_ID).with(createPrincipal(USER_ID))
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(PATCH_CLIENT_JSON_BODY))
                    .hasStatus(404)
                    .bodyJson()
                    .extractingPath("$.error").asString()
                    .isEqualTo(new ClientNotFoundException(CLIENT_ID).getMessage());

            verify(clientMapper).fromDTO(
                    eq(patchClientRequestDTO),
                    eq(USER_ID));
            verify(clientService).patchClient(
                    eq(USER_ID),
                    eq(CLIENT_ID),
                    eq(patchClientRequest));
        }

        @Test
        void rejectsEmptyName() {
            final String jsonBody = """
                    {
                        "name": ""
                    }""";

            assertThat(mockMvc.patch().uri(URL, CLIENT_ID).with(createPrincipal(USER_ID))
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(jsonBody))
                    .hasStatus(400)
                    .bodyJson()
                    .extractingPath("$.error").asString().isEqualTo(CLIENT_NAME_NULL_OR_EMPTY_ERROR_MESSAGE);

            verifyNoInteractions(clientMapper, clientService);
        }

        @Test
        void rejectsBlankName() {
            final String jsonBody = """
                    {
                        "name": "  "
                    }""";

            assertThat(mockMvc.patch().uri(URL, CLIENT_ID).with(createPrincipal(USER_ID))
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(jsonBody))
                    .hasStatus(400)
                    .bodyJson()
                    .extractingPath("$.error").asString().isEqualTo(CLIENT_NAME_NULL_OR_EMPTY_ERROR_MESSAGE);

            verifyNoInteractions(clientMapper, clientService);
        }
    }

    @Nested
    class DeleteClient {
        private static final String URL = "/api/v1/clients/{clientId}";

        @Test
        void deletesClient() {
            assertThat(mockMvc.delete().uri(URL, CLIENT_ID).with(createPrincipal(USER_ID)))
                    .hasStatus(204);

            verify(clientService).deleteClient(USER_ID, CLIENT_ID);
        }

        @Test
        void rejectsUnauthenticatedRequest() {
            assertThat(mockMvc.delete().uri(URL, CLIENT_ID)).hasStatus(401);

            verifyNoInteractions(clientService);
        }

        @Test
        void rejectsNonExistentClient() {
            doThrow(new ClientNotFoundException(CLIENT_ID)).when(clientService).deleteClient(USER_ID, CLIENT_ID);

            assertThat(mockMvc.delete().uri(URL, CLIENT_ID).with(createPrincipal(USER_ID)))
                    .hasStatus(404)
                    .bodyJson()
                    .extractingPath("$.error").asString()
                    .isEqualTo(new ClientNotFoundException(CLIENT_ID).getMessage());

            verify(clientService).deleteClient(USER_ID, CLIENT_ID);
        }
    }

    private static Client createClient() {
        final Client client = new Client("MyClient", createUser(USER_ID));
        ReflectionTestUtils.setField(client, "id", CLIENT_ID);
        ReflectionTestUtils.setField(client, "createdAt", CREATED_AT);

        return client;
    }

    private static ClientDTO createClientDTO() {
        return new ClientDTO(CLIENT_ID, "MyClient", USER_ID, CREATED_AT);
    }

    private static String createClientDTOJson() {
        return """
                {
                    "id": %s,
                    "name": "MyClient",
                    "userId": %s,
                    "createdAt": "%s"
                }""".formatted(CLIENT_ID, USER_ID, CREATED_AT);
    }

    private static CreateClientRequest createClientRequest() {
        return new CreateClientRequest("MyClient", createUser(USER_ID));
    }

    private static CreateClientRequestDTO createCreateClientRequestDTO() {
        return new CreateClientRequestDTO("MyClient");
    }

    private static PatchClientRequest createPatchClientRequest() {
        return new PatchClientRequest("MyClient");
    }

    private static PatchClientRequestDTO createPatchClientRequestDTO() {
        return new PatchClientRequestDTO("MyClient");
    }
}
