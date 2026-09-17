package me.eeshe.tempus.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import me.eeshe.tempus.entity.TimeEntry;
import me.eeshe.tempus.request.CreateTimeEntryRequest;
import me.eeshe.tempus.request.PatchTimeEntryRequest;

public interface TimeEntryService {

    Page<TimeEntry> listTimeEntries(Pageable pageable);

    TimeEntry getTimeEntry(long timeEntryId);

    TimeEntry createTimeEntry(CreateTimeEntryRequest createTimeEntryRequest);

    TimeEntry patchTimeEntry(long timeEntryId, PatchTimeEntryRequest patchTimeEntryRequest);

    void deleteTimeEntry(long timeEntryId);
}
