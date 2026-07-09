package me.eeshe.tempus.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import me.eeshe.tempus.dto.CreateGroupRequestDTO;
import me.eeshe.tempus.dto.GroupDTO;
import me.eeshe.tempus.dto.UpdateGroupRequestDTO;
import me.eeshe.tempus.entity.Group;
import me.eeshe.tempus.mapper.GroupMapper;
import me.eeshe.tempus.request.CreateGroupRequest;
import me.eeshe.tempus.request.UpdateGroupRequest;
import me.eeshe.tempus.service.GroupService;

@RestController
@RequestMapping(path = "api/v1/groups")
public class GroupController {
    private final GroupService groupService;
    private final GroupMapper groupMapper;

    public GroupController(GroupService groupService, GroupMapper groupMapper) {
        this.groupService = groupService;
        this.groupMapper = groupMapper;
    }

    @GetMapping
    public ResponseEntity<List<GroupDTO>> listGroups() {
        final List<Group> groups = groupService.listGroups();
        final List<GroupDTO> groupDTOs = groups.stream().map(groupMapper::toDTO).toList();

        return ResponseEntity.ok(groupDTOs);
    }

    @GetMapping(path = "/{groupId}")
    public ResponseEntity<GroupDTO> getGroup(@PathVariable long groupId) {
        return ResponseEntity.ok(groupMapper.toDTO(groupService.getGroup(groupId)));
    }

    @PostMapping
    public ResponseEntity<GroupDTO> createGroup(CreateGroupRequestDTO createGroupRequestDTO) {
        final CreateGroupRequest createGroupRequest = groupMapper.fromDTO(createGroupRequestDTO);
        final Group createdGroup = groupService.createGroup(createGroupRequest);

        return new ResponseEntity<>(groupMapper.toDTO(createdGroup), HttpStatus.CREATED);
    }

    @PutMapping(path = "/{groupId}")
    public ResponseEntity<GroupDTO> updateGroup(
            @PathVariable long groupId,
            UpdateGroupRequestDTO updateGroupRequestDTO) {
        final UpdateGroupRequest updateGroupRequest = groupMapper.fromDTO(updateGroupRequestDTO);
        final Group updatedGroup = groupService.updateGroup(groupId, updateGroupRequest);

        return ResponseEntity.ok(groupMapper.toDTO(updatedGroup));
    }

    @DeleteMapping(path = "/{groupId}")
    public ResponseEntity<Void> deleteGroup(@PathVariable long groupId) {
        groupService.deleteGroup(groupId);

        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}
