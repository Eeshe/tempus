package me.eeshe.tempus.model;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;

public class TimerEntry {
  private final String projectName;
  private final String clientName;
  private final String description;
  private final String task;
  private final String email;
  private final List<String> tags;
  private final boolean billable;
  private final long startTimeMillis;
  private final long durationMillis;

  public TimerEntry(
      String projectName,
      String clientName,
      String description,
      String task,
      String email,
      List<String> tags,
      boolean billable,
      long startTimeMillis,
      long durationMillis) {
    this.projectName = projectName;
    this.clientName = clientName;
    this.description = description;
    this.task = task;
    this.email = email;
    this.tags = tags;
    this.billable = billable;
    this.startTimeMillis = startTimeMillis;
    this.durationMillis = durationMillis;
  }

  /**
   * Generes a String with the format 'ProjectName:Task'. If there is no task,
   * it will simply be 'ProjectName'.
   */
  public String createProjectTaskString() {
    String projectTaskString = projectName;
    if (task == null || task.isEmpty()) {
      return projectTaskString;
    }
    return projectTaskString + ":" + task;
  }

  public String getProjectName() {
    return projectName;
  }

  public String getClientName() {
    return clientName;
  }

  public String getDescription() {
    return description;
  }

  public String getTask() {
    return task;
  }

  public String getEmail() {
    return email;
  }

  public List<String> getTags() {
    return tags;
  }

  public boolean isBillable() {
    return billable;
  }

  public long getStartTimeMillis() {
    return startTimeMillis;
  }

  public LocalDateTime getStartDateTime() {
    return Instant.ofEpochMilli(startTimeMillis).atZone(ZoneId.systemDefault()).toLocalDateTime();
  }

  public long getDurationMillis() {
    return durationMillis;
  }
}
