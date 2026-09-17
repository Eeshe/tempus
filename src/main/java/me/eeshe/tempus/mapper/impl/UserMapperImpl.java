package me.eeshe.tempus.mapper.impl;

import org.springframework.stereotype.Component;

import me.eeshe.tempus.dto.CreateUserRequestDTO;
import me.eeshe.tempus.dto.PatchUserRequestDTO;
import me.eeshe.tempus.dto.UpdateUserRequestDTO;
import me.eeshe.tempus.dto.UserDTO;
import me.eeshe.tempus.entity.User;
import me.eeshe.tempus.mapper.UserMapper;
import me.eeshe.tempus.request.CreateUserRequest;
import me.eeshe.tempus.request.PatchUserRequest;
import me.eeshe.tempus.request.UpdateUserRequest;

@Component
public class UserMapperImpl implements UserMapper {

    @Override
    public UserDTO toDTO(User user) {
        return new UserDTO(
                user.getId(),
                user.getName(),
                user.getCreatedAt());
    }

    @Override
    public CreateUserRequest fromDTO(CreateUserRequestDTO createUserRequestDTO) {
        return new CreateUserRequest(
                createUserRequestDTO.name(),
                createUserRequestDTO.password());
    }

    @Override
    public UpdateUserRequest fromDTO(UpdateUserRequestDTO updateUserRequestDTO) {
        return new UpdateUserRequest(
                updateUserRequestDTO.name(),
                updateUserRequestDTO.password());
    }

    @Override
    public PatchUserRequest fromDTO(PatchUserRequestDTO patchUserRequestDTO) {
        return new PatchUserRequest(
                patchUserRequestDTO.name(),
                patchUserRequestDTO.password());
    }
}
