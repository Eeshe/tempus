package me.eeshe.tempus.service.impl;

import java.util.List;

import org.springframework.stereotype.Service;

import me.eeshe.tempus.entity.TimeEntry;
import me.eeshe.tempus.repository.TimeEntryRepository;
import me.eeshe.tempus.service.TimeEntryService;

@Service
public class TimeEntryServiceImpl implements TimeEntryService {
    private final TimeEntryRepository timeEntryRepository;

    public TimeEntryServiceImpl(TimeEntryRepository timeEntryRepository) {
        this.timeEntryRepository = timeEntryRepository;
    }

    @Override
    public void saveTimeEntry(TimeEntry timeEntry) {
        timeEntryRepository.save(timeEntry);
    }

    @Override
    public void updateTimeEntry(TimeEntry timeEntry) {
        timeEntryRepository.save(timeEntry);
    }

    @Override
    public void deleteTimeEntry(long timeEntryId) {
        final TimeEntry timeEntry = getTimeEntryById(timeEntryId);
        if (timeEntry == null) {
            return;
        }
        timeEntryRepository.deleteById(timeEntry.getId());
    }

    @Override
    public TimeEntry getTimeEntryById(long timeEntryId) {
        // TODO: Raise exception if it's not found
        return timeEntryRepository.findById(timeEntryId);
    }

    @Override
    public List<TimeEntry> getAllTimeEntries() {
        return timeEntryRepository.findAll();
    }
}
