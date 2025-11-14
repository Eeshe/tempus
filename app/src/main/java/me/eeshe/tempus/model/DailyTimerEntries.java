package me.eeshe.tempus.model;

import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class DailyTimerEntries {
  private final LocalDate date;
  private final Map<String, List<TimerEntry>> timerEntries;

  public DailyTimerEntries(LocalDate localDate) {
    this.date = localDate;
    this.timerEntries = new LinkedHashMap<>();
  }

  public DailyTimerEntries(LocalDate localDate, Map<String, List<TimerEntry>> timerEntries) {
    this.date = localDate;
    this.timerEntries = timerEntries;
  }

  public long computeElapsedTimeMillis() {
    return timerEntries.values().stream().flatMap(List::stream)
        .map(TimerEntry::getDurationMillis).mapToLong(Long::longValue).sum();
  }

  public LocalDate getDate() {
    return date;
  }

  public Map<String, List<TimerEntry>> getTimerEntries() {
    return timerEntries;
  }
}
