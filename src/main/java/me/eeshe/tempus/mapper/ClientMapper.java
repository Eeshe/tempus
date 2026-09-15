package me.eeshe.tempus.mapper;

import me.eeshe.tempus.dto.ClientDTO;
import me.eeshe.tempus.dto.CreateClientRequestDTO;
import me.eeshe.tempus.dto.PatchClientRequestDTO;
import me.eeshe.tempus.entity.Client;
import me.eeshe.tempus.request.CreateClientRequest;
import me.eeshe.tempus.request.PatchClientRequest;

public interface ClientMapper {

    ClientDTO toDTO(Client client);

    CreateClientRequest fromDTO(CreateClientRequestDTO createClientRequestDTO, long userId);

    PatchClientRequest fromDTO(PatchClientRequestDTO patchClientRequestDTO);
}
