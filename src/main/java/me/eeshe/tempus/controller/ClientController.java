package me.eeshe.tempus.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import me.eeshe.tempus.dto.ClientDTO;
import me.eeshe.tempus.dto.CreateClientRequestDTO;
import me.eeshe.tempus.dto.PatchClientRequestDTO;
import me.eeshe.tempus.entity.Client;
import me.eeshe.tempus.mapper.ClientMapper;
import me.eeshe.tempus.request.CreateClientRequest;
import me.eeshe.tempus.request.PatchClientRequest;
import me.eeshe.tempus.security.UserDetailsImpl;
import me.eeshe.tempus.service.ClientService;

@RestController
@RequestMapping(path = "api/v1/clients")
public class ClientController {
    private final ClientService clientService;
    private final ClientMapper clientMapper;

    public ClientController(ClientService clientService, ClientMapper clientMapper) {
        this.clientService = clientService;
        this.clientMapper = clientMapper;
    }

    @GetMapping
    public ResponseEntity<List<ClientDTO>> listClients() {
        final List<Client> clients = clientService.listClients();
        final List<ClientDTO> clientDTOs = clients.stream().map(clientMapper::toDTO).toList();

        return ResponseEntity.ok(clientDTOs);
    }

    @GetMapping(path = "/{clientId}")
    public ResponseEntity<ClientDTO> getClient(@PathVariable long clientId) {
        final Client client = clientService.getClient(clientId);
        final ClientDTO clientDTO = clientMapper.toDTO(client);

        return ResponseEntity.ok(clientDTO);
    }

    @PostMapping
    public ResponseEntity<ClientDTO> createClient(
            @Valid @RequestBody CreateClientRequestDTO createClientRequestDTO,
            @AuthenticationPrincipal UserDetailsImpl userDetails) {
        final CreateClientRequest createClientRequest = clientMapper.fromDTO(createClientRequestDTO,
                userDetails.getId());
        final Client createdClient = clientService.createClient(createClientRequest);
        final ClientDTO createdClientDTO = clientMapper.toDTO(createdClient);

        return new ResponseEntity<>(createdClientDTO, HttpStatus.CREATED);
    }

    @PatchMapping(path = "/{clientId}")
    public ResponseEntity<ClientDTO> patchClient(
            @PathVariable long clientId,
            @Valid @RequestBody PatchClientRequestDTO patchClientRequestDTO) {
        final PatchClientRequest patchClientRequest = clientMapper.fromDTO(patchClientRequestDTO);
        final Client patchedClient = clientService.patchClient(clientId, patchClientRequest);
        final ClientDTO patchedClientDTO = clientMapper.toDTO(patchedClient);

        return ResponseEntity.ok(patchedClientDTO);
    }

    @DeleteMapping(path = "/{clientId}")
    public ResponseEntity<Void> deleteClient(@PathVariable long clientId) {
        clientService.deleteClient(clientId);

        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}
