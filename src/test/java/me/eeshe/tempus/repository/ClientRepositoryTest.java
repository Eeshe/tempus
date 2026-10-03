package me.eeshe.tempus.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import me.eeshe.tempus.entity.Client;
import me.eeshe.tempus.entity.Project;
import me.eeshe.tempus.entity.User;
import me.eeshe.tempus.support.RepositoryTestBase;

public class ClientRepositoryTest extends RepositoryTestBase {

    @Nested
    class FindByUserId {

        @Test
        void returnsUserClients() {
            final User user = createUser("MyUser");
            final User otherUser = createUser("MyOtherUser");

            final Client firstClient = createClient("MyClient", user);
            final Client secondClient = createClient("MySecondClient", user);

            createClient("MyOtherClient", otherUser);

            final List<Client> clients = clientRepository.findByUserId(user.getId());

            assertThat(clients).containsExactlyInAnyOrder(firstClient, secondClient);
        }

        @Test
        void returnsEmptyWhenUserHasNoClients() {
            final User user = createUser("MyUser");

            assertThat(clientRepository.findByUserId(user.getId())).isEmpty();
        }
    }

    @Nested
    class FindByIdAndUserId {

        @Test
        void returnsClientWhenOwnedByUser() {
            final User user = createUser("MyUser");
            final Client client = createClient("MyClient", user);

            final Optional<Client> found = clientRepository.findByIdAndUserId(client.getId(), user.getId());

            assertThat(found).contains(client);
        }

        @Test
        void returnsClientWhenBothUsersHaveSameClientName() {
            final User user = createUser("MyUser");
            final User otherUser = createUser("MyOtherUser");

            final Client client = createClient("MyClient", user);

            createClient("MyClient", otherUser);

            final Optional<Client> found = clientRepository.findByIdAndUserId(client.getId(), user.getId());

            assertThat(found).contains(client);
        }

        @Test
        void returnsEmptyWhenOwnedByOtherUser() {
            final User user = createUser("MyUser");
            final User otherUser = createUser("MyOtherUser");

            final Client client = createClient("MyClient", user);

            assertThat(clientRepository.findByIdAndUserId(client.getId(), otherUser.getId())).isEmpty();
        }

        @Test
        void returnsEmptyWhenClientDoesNotExist() {
            final User user = createUser("MyUser");

            assertThat(clientRepository.findByIdAndUserId(9999L, user.getId())).isEmpty();
        }

        @Test
        void returnsEmptyWhenUserDoesNotExist() {
            final User user = createUser("MyUser");
            final Client client = createClient("MyClient", user);

            assertThat(clientRepository.findByIdAndUserId(client.getId(), 99999)).isEmpty();
        }
    }

    @Nested
    class FindByUserIdAndName {

        @Test
        void returnsClientWhenNameMatches() {
            final User user = createUser("MyUser");
            final Client client = createClient("MyClient", user);

            final Optional<Client> found = clientRepository.findByUserIdAndName(user.getId(), "MyClient");

            assertThat(found).contains(client);
        }

        @Test
        void returnsUserClientWhenOtherUserHasSameClientName() {
            final User user = createUser("MyUser");
            final User otherUser = createUser("MyOtherUser");

            final Client client = createClient("MyClient", user);

            createClient("MyClient", otherUser);

            final Optional<Client> found = clientRepository.findByUserIdAndName(user.getId(), "MyClient");

            assertThat(found).contains(client);
        }

        @Test
        void returnsEmptyWhenNameDiffersOnlyByCase() {
            final User user = createUser("MyUser");
            createClient("MyClient", user);

            assertThat(clientRepository.findByUserIdAndName(user.getId(), "myclient")).isEmpty();
        }

        @Test
        void returnsEmptyWhenNameDoesNotMatch() {
            final User user = createUser("MyUser");
            createClient("MyClient", user);

            assertThat(clientRepository.findByUserIdAndName(user.getId(), "MyOtherClient")).isEmpty();
        }

        @Test
        void returnsEmptyWhenOwnedByOtherUser() {
            final User user = createUser("MyUser");
            final User otherUser = createUser("MyOtherUser");

            createClient("MyClient", user);

            assertThat(clientRepository.findByUserIdAndName(otherUser.getId(), "MyClient")).isEmpty();
        }
    }

    @Nested
    class SaveClient {

        @Test
        void savePersistsClientAndSetsGeneratedFields() {
            final User user = createUser("MyUser");

            final Client saved = clientRepository.save(new Client("MyClient", user));

            assertThat(saved.getId()).isPositive();
            assertThat(saved.getCreatedAt()).isNotNull();
        }
    }

    @Nested
    class DeleteClient {

        @Test
        void deleteRemovesClient() {
            final User user = createUser("MyUser");
            final Client client = createClient("MyClient", user);

            clientRepository.deleteById(client.getId());

            assertThat(clientRepository.findByIdAndUserId(client.getId(), user.getId())).isEmpty();
        }

        @Test
        void deleteSetsCorrespondingProjectClientsToNull() {
            final User user = createUser("MyUser");
            final Client client = createClient("MyClient", user);
            final Project project = createProject("MyProject", user, null, client);

            entityManager.flush();
            entityManager.clear();

            clientRepository.deleteById(client.getId());
            entityManager.flush();
            entityManager.clear();

            final Optional<Project> foundProject = projectRepository.findById(project.getId());

            assertThat(foundProject).isPresent();
            assertThat(foundProject.get().getClient()).isNull();
        }

        @Test
        void deleteLeavesUserIntact() {
            final User user = createUser("MyUser");
            final Client client = createClient("MyClient", user);

            clientRepository.deleteById(client.getId());
            entityManager.flush();
            entityManager.clear();

            assertThat(userRepository.findById(user.getId())).isPresent();
        }

        @Test
        void deleteByIdForMissingIdIsNoOp() {
            final User user = createUser("MyUser");
            final Client client = createClient("MyClient", user);

            clientRepository.deleteById(9999L);
            entityManager.flush();
            entityManager.clear();

            assertThat(clientRepository.findByIdAndUserId(client.getId(), user.getId())).contains(client);
        }

        @Test
        void deleteLeavesOtherUsersClientsUntouched() {
            final User user = createUser("MyUser");
            final User otherUser = createUser("MyOtherUser");

            final Client client = createClient("MyClient", user);
            final Client otherClient = createClient("MyClient", otherUser);

            clientRepository.deleteById(client.getId());
            entityManager.flush();
            entityManager.clear();

            final Optional<Client> found = clientRepository
                    .findByIdAndUserId(otherClient.getId(), otherUser.getId());

            assertThat(found).contains(otherClient);
        }
    }
}
