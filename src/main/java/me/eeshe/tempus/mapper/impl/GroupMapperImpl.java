package me.eeshe.tempus.mapper.impl;

import org.springframework.stereotype.Component;

import me.eeshe.tempus.dto.CreateGroupRequestDTO;
import me.eeshe.tempus.dto.GroupDTO;
import me.eeshe.tempus.dto.PatchGroupRequestDTO;
import me.eeshe.tempus.dto.UpdateGroupRequestDTO;
import me.eeshe.tempus.entity.Group;
import me.eeshe.tempus.entity.User;
import me.eeshe.tempus.mapper.GroupMapper;
import me.eeshe.tempus.request.CreateGroupRequest;
import me.eeshe.tempus.request.PatchGroupRequest;
import me.eeshe.tempus.request.UpdateGroupRequest;
import me.eeshe.tempus.service.UserService;

@Component
public class GroupMapperImpl implements GroupMapper {
    private final UserService userService;

    public GroupMapperImpl(UserService userService) {
        this.userService = userService;
    }

    @Override
    public GroupDTO toDTO(Group group) {
        return new GroupDTO(
                group.getId(),
                group.getName(),
                group.getUsers().stream().map(User::getId).toList(),
                group.getCreatedAt());
    }

    @Override
    public CreateGroupRequest fromDTO(CreateGroupRequestDTO createGroupRequestDTO) {
        return new CreateGroupRequest(createGroupRequestDTO.name());
    }

    @Override
    public UpdateGroupRequest fromDTO(UpdateGroupRequestDTO updateGroupRequestDTO) {
        return new UpdateGroupRequest(
                updateGroupRequestDTO.name(),
                updateGroupRequestDTO.userIds().stream().map(userService::getUser).toList());
    }

    @Override
    public PatchGroupRequest fromDTO(PatchGroupRequestDTO patchGroupRequestDTO) {
        return new PatchGroupRequest(
                patchGroupRequestDTO.name(),
                patchGroupRequestDTO.userIds().map(userIds -> userIds.stream()
                        .map(userService::getUser).toList()));
    }
}
