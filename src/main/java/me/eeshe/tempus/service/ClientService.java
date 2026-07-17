package me.eeshe.tempus.service;

import java.util.List;

import me.eeshe.tempus.entity.Client;
import me.eeshe.tempus.request.CreateClientRequest;
import me.eeshe.tempus.request.PatchClientRequest;

public interface ClientService {

    List<Client> listClients();

    Client getClient(long clientId);

    Client createClient(CreateClientRequest createClientRequest);

    Client patchClient(long clientId, PatchClientRequest patchClientRequest);

    void deleteClient(long clientId);
}
