package me.eeshe.tempus.mapper;

import me.eeshe.tempus.dto.CreateUserRequestDTO;
import me.eeshe.tempus.dto.UpdateUserRequestDTO;
import me.eeshe.tempus.dto.UserDTO;
import me.eeshe.tempus.entity.User;
import me.eeshe.tempus.request.CreateUserRequest;
import me.eeshe.tempus.request.UpdateUserRequest;

public interface UserMapper {

    UserDTO toDTO(User user);

    CreateUserRequest fromDTO(CreateUserRequestDTO createUserRequestDTO);

    UpdateUserRequest fromDTO(UpdateUserRequestDTO updateUserRequestDTO);
}
