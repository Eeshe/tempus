package me.eeshe.tempus.exception;

public class UserProjectAlreadyExistsException extends RuntimeException {
    private static final String ERROR_MESSAGE = "User %s already has a project named %s";

    private final long userId;
    private final String projectName;

    public UserProjectAlreadyExistsException(long userId, String projectName) {
        super(String.format(ERROR_MESSAGE, userId, projectName));

        this.userId = userId;
        this.projectName = projectName;
    }

    public long getUserId() {
        return userId;
    }

    public String getProjectName() {
        return projectName;
    }
}
