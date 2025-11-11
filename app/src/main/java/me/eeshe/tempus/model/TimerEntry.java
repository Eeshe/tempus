package me.eeshe.tempus.model;

import java.time.LocalDateTime;
import java.util.List;

public class TimerEntry {
  private final String projectName;
  private final String clientName;
  private final String description;
  private final String task;
  private final String email;
  private final List<String> tags;
  private final boolean billable;
  private final long startTimeMilliseconds;
  private final long durationMillis;

  public TimerEntry(
      String projectName,
      String clientName,
      String description,
      String task,
      String email,
      List<String> tags,
      boolean billable,
      long startTimeMilliseconds,
      long durationMillis) {
    this.projectName = projectName;
    this.clientName = clientName;
    this.description = description;
    this.task = task;
    this.email = email;
    this.tags = tags;
    this.billable = billable;
    this.startTimeMilliseconds = startTimeMilliseconds;
    this.durationMillis = durationMillis;
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

  public long getStartTimeMilliseconds() {
    return startTimeMilliseconds;
  }

  public long getDurationMillis() {
    return durationMillis;
  }
}
