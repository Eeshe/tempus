package me.eeshe.tempus.exception;

public class TaskNotFoundException extends RuntimeException {
    private static final String ERROR_MESSAGE = "Task with ID %s does not exist";

    private final long taskId;

    public TaskNotFoundException(long taskId) {
        super(String.format(ERROR_MESSAGE, taskId));

        this.taskId = taskId;
    }

    public long getTaskId() {
        return taskId;
    }
}
