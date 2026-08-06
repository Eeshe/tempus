package me.eeshe.tempus.mapper.impl;

import org.springframework.stereotype.Component;

import me.eeshe.tempus.dto.CreateUserRequestDTO;
import me.eeshe.tempus.dto.PatchUserRequestDTO;
import me.eeshe.tempus.dto.UpdateUserRequestDTO;
import me.eeshe.tempus.dto.UserDTO;
import me.eeshe.tempus.entity.Group;
import me.eeshe.tempus.entity.User;
import me.eeshe.tempus.mapper.UserMapper;
import me.eeshe.tempus.request.CreateUserRequest;
import me.eeshe.tempus.request.PatchUserRequest;
import me.eeshe.tempus.request.UpdateUserRequest;
import me.eeshe.tempus.service.GroupService;

@Component
public class UserMapperImpl implements UserMapper {
    private final GroupService groupService;

    public UserMapperImpl(GroupService groupService) {
        this.groupService = groupService;
    }

    @Override
    public UserDTO toDTO(User user) {
        return new UserDTO(
                user.getId(),
                user.getName(),
                user.getGroups().stream().map(Group::getId).toList(),
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
                updateUserRequestDTO.password(),
                updateUserRequestDTO.groupIds().stream().map(groupService::getGroup).toList());
    }

    @Override
    public PatchUserRequest fromDTO(PatchUserRequestDTO patchUserRequestDTO) {
        return new PatchUserRequest(
                patchUserRequestDTO.name(),
                patchUserRequestDTO.password(),
                patchUserRequestDTO.groupIds().map(groupIds -> groupIds.stream()
                        .map(groupService::getGroup).toList()));
    }
}
