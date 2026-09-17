package me.eeshe.tempus.repository.projection;

public interface DailyEntryCount {

    Long getEpochDay();

    Long getEntryCount();
}
