package me.eeshe.tempus.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import me.eeshe.tempus.entity.Client;
import me.eeshe.tempus.entity.User;
import me.eeshe.tempus.exception.ClientNotFoundException;
import me.eeshe.tempus.exception.UserClientAlreadyExistsException;
import me.eeshe.tempus.repository.ClientRepository;
import me.eeshe.tempus.request.CreateClientRequest;
import me.eeshe.tempus.request.PatchClientRequest;
import me.eeshe.tempus.support.EntityTestBase;

@ExtendWith(MockitoExtension.class)
public class ClientServiceImplTest extends EntityTestBase {

    @Mock
    private ClientRepository clientRepository;

    @InjectMocks
    private ClientServiceImpl clientService;

    @Nested
    class ListClients {

        @Test
        void returnsAllUserClients() {
            final Client firstClient = createClient();
            final Client secondClient = createSecondClient();

            when(clientRepository.findByUserId(USER_ID)).thenReturn(List.of(firstClient, secondClient));

            final List<Client> clients = clientService.listClients(USER_ID);

            assertThat(clients).containsExactly(firstClient, secondClient);
            verify(clientRepository).findByUserId(USER_ID);
        }
    }

    @Nested
    class GetClient {

        @Test
        void returnsClientWhenFound() {
            final Client client = createClient();

            when(clientRepository.findByIdAndUserId(CLIENT_ID, USER_ID)).thenReturn(Optional.of(client));

            assertThat(clientService.getClient(USER_ID, CLIENT_ID)).isEqualTo(client);
        }

        @Test
        void throwsWhenClientNotFound() {
            when(clientRepository.findByIdAndUserId(CLIENT_ID, USER_ID)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> clientService.getClient(USER_ID, CLIENT_ID))
                    .isInstanceOf(ClientNotFoundException.class)
                    .hasMessage("Client with ID 10 does not exist");
        }
    }

    @Nested
    class GetClientByName {

        @Test
        void returnsClientWhenNameMatches() {
            final Client client = createClient();

            when(clientRepository.findByUserIdAndName(USER_ID, "MyClient")).thenReturn(Optional.of(client));

            assertThat(clientService.getClient(USER_ID, "MyClient")).contains(client);
        }

        @Test
        void returnsEmptyWhenClientDoesNotExist() {
            when(clientRepository.findByUserIdAndName(USER_ID, "MyClient")).thenReturn(Optional.empty());

            assertThat(clientService.getClient(USER_ID, "MyClient")).isEmpty();
        }
    }

    @Nested
    class CreateClient {

        @Test
        void savesClientWithRequestFields() {
            final User user = createUser(USER_ID);
            final CreateClientRequest createClientRequest = new CreateClientRequest("MyClient", user);
            final Client savedClient = createClient();

            when(clientRepository.findByUserIdAndName(USER_ID, "MyClient")).thenReturn(Optional.empty());
            when(clientRepository.save(any(Client.class))).thenReturn(savedClient);

            final Client client = clientService.createClient(createClientRequest);

            final ArgumentCaptor<Client> clientCaptor = ArgumentCaptor.forClass(Client.class);

            verify(clientRepository).save(clientCaptor.capture());

            assertThat(clientCaptor.getValue().getName()).isEqualTo("MyClient");
            assertThat(clientCaptor.getValue().getUser()).isEqualTo(user);
            assertThat(client).isEqualTo(savedClient);
        }

        @Test
        void throwsWhenUserClientAlreadyExists() {
            final User user = createUser(USER_ID);
            final Client existingClient = createClient();

            when(clientRepository.findByUserIdAndName(USER_ID, "MyClient"))
                    .thenReturn(Optional.of(existingClient));

            assertThatThrownBy(() -> clientService.createClient(new CreateClientRequest("MyClient", user)))
                    .isInstanceOf(UserClientAlreadyExistsException.class)
                    .hasMessage("User 1 already has a client named MyClient");

            verify(clientRepository, never()).save(any());
        }
    }

    @Nested
    class PatchClient {

        @Test
        void patchesNameAndSaves() {
            final Client client = createClient();

            when(clientRepository.findByIdAndUserId(CLIENT_ID, USER_ID)).thenReturn(Optional.of(client));
            when(clientRepository.save(client)).thenReturn(client);

            final Client patchedClient = clientService.patchClient(
                    USER_ID, CLIENT_ID, new PatchClientRequest("MyNewClient"));

            assertThat(patchedClient.getName()).isEqualTo("MyNewClient");

            verify(clientRepository).save(client);
        }

        @Test
        void patchesNothingWhenNameNull() {
            final Client client = createClient();

            when(clientRepository.findByIdAndUserId(CLIENT_ID, USER_ID)).thenReturn(Optional.of(client));
            when(clientRepository.save(client)).thenReturn(client);

            final Client patchedClient = clientService.patchClient(USER_ID, CLIENT_ID, new PatchClientRequest(null));

            assertThat(patchedClient.getName()).isEqualTo("MyClient");

            verify(clientRepository).save(client);
        }

        @Test
        void throwsWhenClientNotFound() {
            when(clientRepository.findByIdAndUserId(CLIENT_ID, USER_ID)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> clientService.patchClient(
                    USER_ID, CLIENT_ID, new PatchClientRequest("MyNewClient")))
                    .isInstanceOf(ClientNotFoundException.class)
                    .hasMessage("Client with ID 10 does not exist");

            verify(clientRepository, never()).save(any());
        }
    }

    @Nested
    class DeleteClient {

        @Test
        void deletesClientWhenFound() {
            final Client client = createClient();

            when(clientRepository.findByIdAndUserId(CLIENT_ID, USER_ID)).thenReturn(Optional.of(client));

            clientService.deleteClient(USER_ID, CLIENT_ID);

            verify(clientRepository).deleteById(CLIENT_ID);
        }

        @Test
        void throwsWhenClientNotFound() {
            when(clientRepository.findByIdAndUserId(CLIENT_ID, USER_ID)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> clientService.deleteClient(USER_ID, CLIENT_ID))
                    .isInstanceOf(ClientNotFoundException.class)
                    .hasMessage("Client with ID 10 does not exist");

            verify(clientRepository, never()).deleteById(anyLong());
        }
    }
}
