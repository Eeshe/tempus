package me.eeshe.tempus.service;

import java.util.List;

import me.eeshe.tempus.entity.TimeEntry;
import me.eeshe.tempus.request.CreateTimeEntryRequest;
import me.eeshe.tempus.request.UpdateTimeEntryRequest;

public interface TimeEntryService {

    TimeEntry getTimeEntry(long timeEntryId);

    List<TimeEntry> getAllTimeEntries();

    TimeEntry saveTimeEntry(CreateTimeEntryRequest createTimeEntryRequest);

    TimeEntry updateTimeEntry(UpdateTimeEntryRequest updadTimeEntryRequest);

    void deleteTimeEntry(long timeEntryId);
}
