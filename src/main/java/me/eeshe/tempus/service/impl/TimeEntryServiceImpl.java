package me.eeshe.tempus.service.impl;

import java.util.List;

import org.springframework.stereotype.Service;

import me.eeshe.tempus.entity.TimeEntry;
import me.eeshe.tempus.repository.TimeEntryRepository;
import me.eeshe.tempus.request.CreateTimeEntryRequest;
import me.eeshe.tempus.request.UpdateTimeEntryRequest;
import me.eeshe.tempus.service.TimeEntryService;

@Service
public class TimeEntryServiceImpl implements TimeEntryService {
    private final TimeEntryRepository timeEntryRepository;

    public TimeEntryServiceImpl(TimeEntryRepository timeEntryRepository) {
        this.timeEntryRepository = timeEntryRepository;
    }

    @Override
    public TimeEntry getTimeEntry(long timeEntryId) {
        // TODO: Raise exception if it's not found
        return timeEntryRepository.findById(timeEntryId);
    }

    @Override
    public List<TimeEntry> getAllTimeEntries() {
        return timeEntryRepository.findAll();
    }

    @Override
    public TimeEntry saveTimeEntry(CreateTimeEntryRequest createTimeEntryRequest) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'saveTimeEntry'");
    }

    @Override
    public TimeEntry updateTimeEntry(UpdateTimeEntryRequest updadTimeEntryRequest) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'updateTimeEntry'");
    }

    @Override
    public void deleteTimeEntry(long timeEntryId) {
        timeEntryRepository.deleteById(timeEntryId);
    }
}
