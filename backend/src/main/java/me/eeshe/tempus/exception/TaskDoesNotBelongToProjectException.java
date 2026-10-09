package me.eeshe.tempus.exception;

public class TaskDoesNotBelongToProjectException extends RuntimeException {
    private static final String ERROR_MESSAGE = "Task with ID %s is not from project with ID %s";

    private final long taskId;
    private final long projectId;

    public TaskDoesNotBelongToProjectException(long projectId, long taskId) {
        super(String.format(ERROR_MESSAGE, taskId, projectId));

        this.taskId = taskId;
        this.projectId = projectId;
    }

    public long getTaskId() {
        return taskId;
    }

    public long getProjectId() {
        return projectId;
    }
}
