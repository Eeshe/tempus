package me.eeshe.tempus.service.impl;

import java.util.List;

import org.springframework.stereotype.Service;

import me.eeshe.tempus.entity.Group;
import me.eeshe.tempus.exception.GroupNotFoundException;
import me.eeshe.tempus.repository.GroupRepository;
import me.eeshe.tempus.request.CreateGroupRequest;
import me.eeshe.tempus.request.UpdateGroupRequest;
import me.eeshe.tempus.service.GroupService;

@Service
public class GroupServiceImpl implements GroupService {
    private final GroupRepository groupRepository;

    public GroupServiceImpl(GroupRepository groupRepository) {
        this.groupRepository = groupRepository;
    }

    @Override
    public List<Group> listGroups() {
        return groupRepository.findAll();
    }

    @Override
    public Group getGroup(long groupId) {
        return groupRepository.findById(groupId).orElseThrow(() -> new GroupNotFoundException(groupId));
    }

    @Override
    public Group createGroup(CreateGroupRequest createGroupRequest) {
        return groupRepository.save(new Group(createGroupRequest.name()));
    }

    @Override
    public Group updateGroup(long groupId, UpdateGroupRequest updateGroupRequest) {
        final Group group = getGroup(groupId);
        if (updateGroupRequest.name() != null) {
            group.setName(updateGroupRequest.name());
        }
        return groupRepository.save(group);
    }

    @Override
    public void deleteGroup(long groupId) {
        groupRepository.deleteById(groupId);
    }
}
