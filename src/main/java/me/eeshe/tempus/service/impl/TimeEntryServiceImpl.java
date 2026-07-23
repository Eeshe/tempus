package me.eeshe.tempus.service.impl;

import java.util.List;

import org.springframework.stereotype.Service;

import me.eeshe.tempus.entity.TimeEntry;
import me.eeshe.tempus.exception.TimeEntryNotFoundException;
import me.eeshe.tempus.repository.TimeEntryRepository;
import me.eeshe.tempus.request.CreateTimeEntryRequest;
import me.eeshe.tempus.request.PatchTimeEntryRequest;
import me.eeshe.tempus.service.TimeEntryService;

@Service
public class TimeEntryServiceImpl implements TimeEntryService {
    private final TimeEntryRepository timeEntryRepository;

    public TimeEntryServiceImpl(TimeEntryRepository timeEntryRepository) {
        this.timeEntryRepository = timeEntryRepository;
    }

    @Override
    public List<TimeEntry> listTimeEntries() {
        return timeEntryRepository.findAll();
    }

    @Override
    public TimeEntry getTimeEntry(long timeEntryId) {
        return timeEntryRepository.findById(timeEntryId).orElseThrow(() -> new TimeEntryNotFoundException(timeEntryId));
    }

    @Override
    public TimeEntry createTimeEntry(CreateTimeEntryRequest createTimeEntryRequest) {
        return timeEntryRepository.save(new TimeEntry(
                createTimeEntryRequest.group(),
                createTimeEntryRequest.user(),
                createTimeEntryRequest.project(),
                createTimeEntryRequest.task(),
                createTimeEntryRequest.description(),
                createTimeEntryRequest.isBillable()));
    }

    @Override
    public TimeEntry patchTimeEntry(long timeEntryId, PatchTimeEntryRequest patchTimeEntryRequest) {
        final TimeEntry timeEntry = getTimeEntry(timeEntryId);

        patchTimeEntryRequest.project().ifPresent(timeEntry::setProject);
        patchTimeEntryRequest.task().ifPresent(timeEntry::setTask);
        patchTimeEntryRequest.description().ifPresent(timeEntry::setDescription);
        patchTimeEntryRequest.isBillable().ifPresent(timeEntry::setBillable);

        return timeEntryRepository.save(timeEntry);
    }

    @Override
    public void deleteTimeEntry(long timeEntryId) {
        timeEntryRepository.deleteById(timeEntryId);
    }

}
