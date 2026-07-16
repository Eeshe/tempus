package me.eeshe.tempus.service;

import java.util.List;

import me.eeshe.tempus.entity.Group;
import me.eeshe.tempus.request.CreateGroupRequest;
import me.eeshe.tempus.request.PatchGroupRequest;
import me.eeshe.tempus.request.UpdateGroupRequest;

public interface GroupService {

    List<Group> listGroups();

    Group getGroup(long groupId);

    Group createGroup(CreateGroupRequest createGroupRequest);

    Group updateGroup(long groupId, UpdateGroupRequest updateGroupRequest);

    Group patchGroup(long groupId, PatchGroupRequest patchGroupRequest);

    void deleteGroup(long groupId);
}
