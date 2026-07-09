package me.eeshe.tempus.exception;

public class GroupNotFoundException extends RuntimeException {
    private static final String ERROR_MESSAGE = "Group with ID %s does not exist";

    private final long groupId;

    public GroupNotFoundException(long groupId) {
        super(String.format(ERROR_MESSAGE, groupId));

        this.groupId = groupId;
    }

    public long getGroupId() {
        return groupId;
    }
}
