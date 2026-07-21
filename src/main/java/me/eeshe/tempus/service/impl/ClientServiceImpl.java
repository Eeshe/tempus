package me.eeshe.tempus.service.impl;

import java.util.List;

import org.springframework.stereotype.Service;

import me.eeshe.tempus.entity.Client;
import me.eeshe.tempus.exception.ClientNotFoundException;
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
    public List<Client> listClients() {
        return clientRepository.findAll();
    }

    @Override
    public Client getClient(long clientId) {
        return clientRepository.findById(clientId).orElseThrow(() -> new ClientNotFoundException(clientId));
    }

    @Override
    public Client createClient(CreateClientRequest createClientRequest) {
        return clientRepository.save(new Client(
                createClientRequest.name(),
                createClientRequest.user(),
                createClientRequest.hourlyRate()));
    }

    @Override
    public Client patchClient(long clientId, PatchClientRequest patchClientRequest) {
        final Client client = getClient(clientId);
        if (patchClientRequest.name() != null) {
            client.setName(patchClientRequest.name());
        }
        patchClientRequest.hourlyRate().ifPresent(client::setHourlyRate);

        return clientRepository.save(client);
    }

    @Override
    public void deleteClient(long clientId) {
        clientRepository.deleteById(clientId);
    }

}
