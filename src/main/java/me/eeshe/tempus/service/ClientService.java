package me.eeshe.tempus.service;

import java.util.List;
import java.util.Optional;

import me.eeshe.tempus.entity.Client;
import me.eeshe.tempus.request.CreateClientRequest;
import me.eeshe.tempus.request.PatchClientRequest;

public interface ClientService {

    List<Client> listClients(long userId);

    Client getClient(long userId, long clientId);

    Optional<Client> getClient(long userId, String clientName);

    Client createClient(CreateClientRequest createClientRequest);

    Client patchClient(long userId, long clientId, PatchClientRequest patchClientRequest);

    void deleteClient(long userId, long clientId);
}
