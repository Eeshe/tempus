package me.eeshe.tempus.service.impl;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import me.eeshe.tempus.entity.Client;
import me.eeshe.tempus.exception.ClientNotFoundException;
import me.eeshe.tempus.exception.UserClientAlreadyExistsException;
import me.eeshe.tempus.repository.ClientRepository;
import me.eeshe.tempus.request.CreateClientRequest;
import me.eeshe.tempus.request.PatchClientRequest;
import me.eeshe.tempus.service.ClientService;

@Service
public class ClientServiceImpl implements ClientService {
    private final ClientRepository clientRepository;

    public ClientServiceImpl(ClientRepository clientRepository) {
        this.clientRepository = clientRepository;
    }

    @Override
    public List<Client> listClients(long userId) {
        return clientRepository.findByUserId(userId);
    }

    @Override
    public Client getClient(long userId, long clientId) {
        return clientRepository.findByIdAndUserId(clientId, userId)
                .orElseThrow(() -> new ClientNotFoundException(clientId));
    }

    @Override
    public Optional<Client> getClient(long userId, String clientName) {
        return clientRepository.findByUserIdAndName(userId, clientName);
    }

    @Override
    public Client createClient(CreateClientRequest createClientRequest) {
        final long userId = createClientRequest.user().getId();
        final String clientName = createClientRequest.name();
        clientRepository.findByUserIdAndName(userId, clientName).ifPresent(project -> {
            throw new UserClientAlreadyExistsException(userId, clientName);
        });
        return clientRepository.save(new Client(
                createClientRequest.name(),
                createClientRequest.user()));
    }

    @Override
    public Client patchClient(long userId, long clientId, PatchClientRequest patchClientRequest) {
        final Client client = getClient(userId, clientId);
        if (patchClientRequest.name() != null) {
            client.setName(patchClientRequest.name());
        }
        return clientRepository.save(client);
    }

    @Override
    public void deleteClient(long userId, long clientId) {
        getClient(userId, clientId);

        clientRepository.deleteById(clientId);
    }
}
