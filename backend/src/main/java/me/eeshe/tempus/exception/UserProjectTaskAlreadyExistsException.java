package me.eeshe.tempus.exception;

public class UserProjectTaskAlreadyExistsException extends RuntimeException {
    private static final String ERROR_MESSAGE = "User %s already has a task named %s for project %s";

    private final long userId;
    private final String taskName;
    private final String projectName;

    public UserProjectTaskAlreadyExistsException(long userId, String taskName, String projectName) {
        super(String.format(ERROR_MESSAGE, userId, taskName, projectName));

        this.userId = userId;
        this.taskName = taskName;
        this.projectName = projectName;
    }

    public long getUserId() {
        return userId;
    }

    public String getProjectName() {
        return projectName;
    }

    public String getTaskName() {
        return taskName;
    }
}
