package me.eeshe.tempus.mapper.impl;

import org.springframework.stereotype.Component;

import me.eeshe.tempus.dto.ClientDTO;
import me.eeshe.tempus.dto.CreateClientRequestDTO;
import me.eeshe.tempus.dto.PatchClientRequestDTO;
import me.eeshe.tempus.entity.Client;
import me.eeshe.tempus.mapper.ClientMapper;
import me.eeshe.tempus.request.CreateClientRequest;
import me.eeshe.tempus.request.PatchClientRequest;
import me.eeshe.tempus.service.UserService;

@Component
public class ClientMapperImpl implements ClientMapper {
    private final UserService userService;

    public ClientMapperImpl(UserService userService) {
        this.userService = userService;
    }

    @Override
    public ClientDTO toDTO(Client client) {
        return new ClientDTO(
                client.getId(),
                client.getName(),
                client.getUser().getId(),
                client.getCreatedAt());
    }

    @Override
    public CreateClientRequest fromDTO(CreateClientRequestDTO createClientRequestDTO, long userId) {
        return new CreateClientRequest(
                createClientRequestDTO.name(),
                userService.getUser(userId));
    }

    @Override
    public PatchClientRequest fromDTO(PatchClientRequestDTO patchClientRequestDTO) {
        return new PatchClientRequest(
                patchClientRequestDTO.name());
    }

}
