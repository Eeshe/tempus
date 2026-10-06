package me.eeshe.tempus.exception;

public class ProjectNotFoundException extends RuntimeException {
    private static final String ERROR_MESSAGE = "Project with ID %s does not exist";

    private final long projectId;

    public ProjectNotFoundException(long projectId) {
        super(String.format(ERROR_MESSAGE, projectId));

        this.projectId = projectId;
    }

    public long getProjectId() {
        return projectId;
    }
}
