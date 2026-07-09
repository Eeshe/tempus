package me.eeshe.tempus.mapper.impl;

import org.springframework.stereotype.Component;

import me.eeshe.tempus.dto.CreateGroupRequestDTO;
import me.eeshe.tempus.dto.GroupDTO;
import me.eeshe.tempus.dto.UpdateGroupRequestDTO;
import me.eeshe.tempus.entity.Group;
import me.eeshe.tempus.mapper.GroupMapper;
import me.eeshe.tempus.request.CreateGroupRequest;
import me.eeshe.tempus.request.UpdateGroupRequest;

@Component
public class GroupMapperImpl implements GroupMapper {

    @Override
    public GroupDTO toDTO(Group group) {
        return new GroupDTO(
                group.getId(),
                group.getName(),
                group.getCreatedAt());
    }

    @Override
    public CreateGroupRequest fromDTO(CreateGroupRequestDTO createGroupRequestDTO) {
        return new CreateGroupRequest(createGroupRequestDTO.name());
    }

    @Override
    public UpdateGroupRequest fromDTO(UpdateGroupRequestDTO updateGroupRequestDTO) {
        return new UpdateGroupRequest(updateGroupRequestDTO.name());
    }

}
