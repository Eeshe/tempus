package me.eeshe.tempus.service;

import java.time.Instant;
import java.util.List;

import me.eeshe.tempus.entity.TimeEntry;
import me.eeshe.tempus.model.TimeEntryPage;
import me.eeshe.tempus.request.CreateTimeEntryRequest;
import me.eeshe.tempus.request.PatchTimeEntryRequest;

public interface TimeEntryService {

    TimeEntryPage listTimeEntries(long userId, Instant cursor, int minPageSize);

    TimeEntry getTimeEntry(long userId, long timeEntryId);

    TimeEntry createTimeEntry(CreateTimeEntryRequest createTimeEntryRequest);

    List<TimeEntry> createTimeEntries(List<CreateTimeEntryRequest> createTimeEntryRequests);

    TimeEntry patchTimeEntry(long userId, long timeEntryId, PatchTimeEntryRequest patchTimeEntryRequest);

    void deleteTimeEntry(long userId, long timeEntryId);
}
