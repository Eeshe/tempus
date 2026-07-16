package me.eeshe.tempus.mapper;

import me.eeshe.tempus.dto.CreateGroupRequestDTO;
import me.eeshe.tempus.dto.GroupDTO;
import me.eeshe.tempus.dto.PatchGroupRequestDTO;
import me.eeshe.tempus.dto.UpdateGroupRequestDTO;
import me.eeshe.tempus.entity.Group;
import me.eeshe.tempus.request.CreateGroupRequest;
import me.eeshe.tempus.request.PatchGroupRequest;
import me.eeshe.tempus.request.UpdateGroupRequest;

public interface GroupMapper {

    GroupDTO toDTO(Group group);

    CreateGroupRequest fromDTO(CreateGroupRequestDTO createGroupRequestDTO);

    UpdateGroupRequest fromDTO(UpdateGroupRequestDTO updateGroupRequestDTO);

    PatchGroupRequest fromDTO(PatchGroupRequestDTO patchGroupRequestDTO);
}
