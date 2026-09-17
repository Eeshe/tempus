package me.eeshe.tempus.service.impl;

import java.time.Instant;
import java.util.List;

import org.springframework.stereotype.Service;

import me.eeshe.tempus.entity.TimeEntry;
import me.eeshe.tempus.exception.TimeEntryNotFoundException;
import me.eeshe.tempus.model.TimeEntryPage;
import me.eeshe.tempus.repository.TimeEntryRepository;
import me.eeshe.tempus.repository.projection.DailyEntryCount;
import me.eeshe.tempus.request.CreateTimeEntryRequest;
import me.eeshe.tempus.request.PatchTimeEntryRequest;
import me.eeshe.tempus.service.TimeEntryService;

@Service
public class TimeEntryServiceImpl implements TimeEntryService {
    private static final long MILLIS_PER_DAY = 86_400_000L;

    private final TimeEntryRepository timeEntryRepository;

    public TimeEntryServiceImpl(TimeEntryRepository timeEntryRepository) {
        this.timeEntryRepository = timeEntryRepository;
    }

    @Override
    public TimeEntryPage listTimeEntries(Instant cursor, int minPageSize) {
        minPageSize = Math.max(1, minPageSize);
        final Instant effectiveCursor = cursor != null ? cursor : Instant.now();
        final long cursorDay = Math.floorDiv(effectiveCursor.toEpochMilli(), MILLIS_PER_DAY);
        final Instant currentCursor = Instant.ofEpochMilli(cursorDay * MILLIS_PER_DAY);

        final List<DailyEntryCount> dayGroups = timeEntryRepository.countAllEntriesByUtcEpochDay();

        final long totalElements = dayGroups.stream().mapToLong(DailyEntryCount::getEntryCount).sum();
        final int totalPages = countPages(dayGroups, minPageSize);

        int startIndex = 0;
        while (startIndex < dayGroups.size() && dayGroups.get(startIndex).getEpochDay() > cursorDay) {
            startIndex++;
        }

        final PageLocation location = pageLocation(dayGroups, startIndex, minPageSize);
        final int page = location.page();
        final Instant previousCursor = location.previousPageStart() < 0
                ? null
                : Instant.ofEpochMilli(dayGroups.get(location.previousPageStart()).getEpochDay() * MILLIS_PER_DAY);

        if (startIndex >= dayGroups.size()) {
            return new TimeEntryPage(
                    List.of(),
                    previousCursor,
                    currentCursor,
                    null,
                    page,
                    minPageSize,
                    totalElements,
                    totalPages,
                    startIndex == 0,
                    true);
        }

        int endIndex = startIndex;
        long accumulated = 0;
        while (endIndex < dayGroups.size() && accumulated < minPageSize) {
            accumulated += dayGroups.get(endIndex).getEntryCount();
            endIndex++;
        }
        final int lastIncluded = endIndex - 1;
        final long oldestDay = dayGroups.get(lastIncluded).getEpochDay();

        final Instant from = Instant.ofEpochMilli(oldestDay * MILLIS_PER_DAY);
        final Instant to = Instant.ofEpochMilli((cursorDay + 1) * MILLIS_PER_DAY);
        final List<TimeEntry> content = timeEntryRepository
                .findByStartTimeGreaterThanEqualAndStartTimeLessThanOrderByStartTimeDesc(from, to);

        final boolean last = lastIncluded == dayGroups.size() - 1;
        final Instant nextCursor = last ? null : Instant.ofEpochMilli((oldestDay - 1) * MILLIS_PER_DAY);

        return new TimeEntryPage(content, previousCursor, currentCursor, nextCursor, page, minPageSize, totalElements, totalPages, page == 0, last);
    }

    private int countPages(List<DailyEntryCount> groups, int minSize) {
        int pages = 0;
        int index = 0;
        while (index < groups.size()) {
            long accumulated = 0;
            while (index < groups.size() && accumulated < minSize) {
                accumulated += groups.get(index).getEntryCount();
                index++;
            }
            pages++;
        }
        return pages;
    }

    private PageLocation pageLocation(List<DailyEntryCount> groups, int target, int minSize) {
        int page = 0;
        int index = 0;
        int previousPageStart = -1;
        while (index < groups.size()) {
            final int pageStart = index;
            long accumulated = 0;
            while (index < groups.size() && accumulated < minSize) {
                accumulated += groups.get(index).getEntryCount();
                index++;
            }
            if (target < index) {
                return new PageLocation(page, previousPageStart);
            }
            previousPageStart = pageStart;
            page++;
        }
        return new PageLocation(page, previousPageStart);
    }

    private record PageLocation(int page, int previousPageStart) {
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
                createTimeEntryRequest.isBillable(),
                createTimeEntryRequest.startTime(),
                createTimeEntryRequest.endTime()));
    }

    @Override
    public TimeEntry patchTimeEntry(long timeEntryId, PatchTimeEntryRequest patchTimeEntryRequest) {
        final TimeEntry timeEntry = getTimeEntry(timeEntryId);

        patchTimeEntryRequest.group().ifPresent(timeEntry::setGroup);
        patchTimeEntryRequest.project().ifPresent(timeEntry::setProject);
        patchTimeEntryRequest.task().ifPresent(timeEntry::setTask);
        patchTimeEntryRequest.description().ifPresent(timeEntry::setDescription);
        patchTimeEntryRequest.isBillable().ifPresent(timeEntry::setBillable);
        patchTimeEntryRequest.startTime().ifPresent(timeEntry::setStartTime);
        patchTimeEntryRequest.endTime().ifPresent(timeEntry::setEndTime);

        return timeEntryRepository.save(timeEntry);
    }

    @Override
    public void deleteTimeEntry(long timeEntryId) {
        timeEntryRepository.deleteById(timeEntryId);
    }
}
