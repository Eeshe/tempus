package me.eeshe.tempus.service;

import java.util.List;

import me.eeshe.tempus.entity.TimeEntry;
import me.eeshe.tempus.request.CreateTimeEntryRequest;
import me.eeshe.tempus.request.PatchTimeEntryRequest;

public interface TimeEntryService {

    List<TimeEntry> getAllTimeEntries();

    TimeEntry getTimeEntry(long timeEntryId);

    TimeEntry createTimeEntry(CreateTimeEntryRequest createTimeEntryRequest);

    TimeEntry patchTimeEntry(long timeEntryId, PatchTimeEntryRequest patchTimeEntryRequest);

    void deleteTimeEntry(long timeEntryId);
}
