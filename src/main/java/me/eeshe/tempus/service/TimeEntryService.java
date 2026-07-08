package me.eeshe.tempus.service;

import java.util.List;

import me.eeshe.tempus.entity.TimeEntry;

public interface TimeEntryService {

    void saveTimeEntry(TimeEntry timeEntry);

    void updateTimeEntry(TimeEntry timeEntry);

    void deleteTimeEntry(long timeEntryId);

    TimeEntry getTimeEntryById(long timeEntryId);

    List<TimeEntry> getAllTimeEntries();
}
