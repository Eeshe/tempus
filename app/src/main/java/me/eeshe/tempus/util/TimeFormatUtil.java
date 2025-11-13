package me.eeshe.tempus.util;

import java.time.Duration;

public class TimeFormatUtil {

  public static String formatMillisecondsToHHMMSS(long milliseconds) {
    if (milliseconds < 0) {
      return "00:00:00";
    }
    Duration duration = Duration.ofMillis(milliseconds);

    long hours = duration.toHours();
    long minutes = duration.toMinutes() % 60; // Remaining minutes after extracting full hours
    long seconds = duration.getSeconds() % 60; // Remaining seconds after extracting full minutes
    return String.format("%02d:%02d:%02d", hours, minutes, seconds);
  }
}
