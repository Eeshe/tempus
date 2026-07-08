package me.eeshe.tempus.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

@Entity
public class TimeEntry {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    private String groupId;
    private String userId;
    private String projectId;
    private String taskId;
    private String description;
    private boolean isBillable;

    public TimeEntry(
            long id,
            String groupId,
            String userId,
            String projectId,
            String taskId,
            String description,
            boolean isBillable) {
        this.id = id;
        this.groupId = groupId;
        this.userId = userId;
        this.projectId = projectId;
        this.taskId = taskId;
        this.description = description;
        this.isBillable = isBillable;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getGroupId() {
        return groupId;
    }

    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getProjectId() {
        return projectId;
    }

    public void setProjectId(String projectId) {
        this.projectId = projectId;
    }

    public String getTaskId() {
        return taskId;
    }

    public void setTaskId(String taskId) {
        this.taskId = taskId;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public boolean isBillable() {
        return isBillable;
    }

    public void setBillable(boolean isBillable) {
        this.isBillable = isBillable;
    }
}
