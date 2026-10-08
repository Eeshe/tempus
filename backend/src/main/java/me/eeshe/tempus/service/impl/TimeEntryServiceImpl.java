package me.eeshe.tempus.service.impl;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import me.eeshe.tempus.entity.TimeEntry;
import me.eeshe.tempus.exception.TimeEntriesNotFoundException;
import me.eeshe.tempus.exception.TimeEntryNotFoundException;
import me.eeshe.tempus.model.TimeEntryPage;
import me.eeshe.tempus.repository.TimeEntryRepository;
import me.eeshe.tempus.repository.projection.DailyEntryCount;
import me.eeshe.tempus.request.CreateTimeEntryRequest;
import me.eeshe.tempus.request.DeleteTimeEntriesRequest;
import me.eeshe.tempus.request.PatchTimeEntriesRequest;
import me.eeshe.tempus.request.PatchTimeEntryRequest;
import me.eeshe.tempus.request.TimeEntryPatch;
import me.eeshe.tempus.service.TimeEntryService;

@Service
public class TimeEntryServiceImpl implements TimeEntryService {
    private static final long MILLIS_PER_DAY = 86_400_000L;

    private final TimeEntryRepository timeEntryRepository;

    public TimeEntryServiceImpl(TimeEntryRepository timeEntryRepository) {
        this.timeEntryRepository = timeEntryRepository;
    }

    @Override
    public TimeEntryPage listTimeEntries(long userId, Instant cursor, int minPageSize) {
        minPageSize = Math.max(1, minPageSize);
        final Instant effectiveCursor = cursor != null ? cursor : Instant.now();
        final long cursorEpoch = Math.floorDiv(effectiveCursor.toEpochMilli(), MILLIS_PER_DAY);
        final Instant currentCursor = Instant.ofEpochMilli(cursorEpoch * MILLIS_PER_DAY);

        final List<DailyEntryCount> dayGroups = timeEntryRepository.countAllEntriesByUtcEpochDay(userId);

        final long totalElements = dayGroups.stream().mapToLong(DailyEntryCount::getEntryCount).sum();
        final int totalPages = countPages(dayGroups, minPageSize);

        int startIndex = 0;
        while (startIndex < dayGroups.size() && dayGroups.get(startIndex).getEpochDay() > cursorEpoch) {
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
        final long oldestDayEpoch = dayGroups.get(lastIncluded).getEpochDay();

        final Instant from = Instant.ofEpochMilli(oldestDayEpoch * MILLIS_PER_DAY);
        final Instant to = Instant.ofEpochMilli((cursorEpoch + 1) * MILLIS_PER_DAY);
        final List<TimeEntry> content = timeEntryRepository
                .findByUserIdAndStartTimeGreaterThanEqualAndStartTimeLessThanOrderByStartTimeDesc(userId, from, to);

        final boolean last = lastIncluded == dayGroups.size() - 1;
        final Instant nextCursor = last ? null : Instant.ofEpochMilli((oldestDayEpoch - 1) * MILLIS_PER_DAY);

        return new TimeEntryPage(
                content,
                previousCursor,
                currentCursor,
                nextCursor,
                page,
                minPageSize,
                totalElements,
                totalPages,
                page == 0,
                last);
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
    public TimeEntry getTimeEntry(long userId, long timeEntryId) {
        return timeEntryRepository.findByIdAndUserId(timeEntryId, userId)
                .orElseThrow(() -> new TimeEntryNotFoundException(timeEntryId));
    }

    @Override
    public TimeEntry createTimeEntry(CreateTimeEntryRequest createTimeEntryRequest) {
        return timeEntryRepository.save(requestToTimeEntry(createTimeEntryRequest));
    }

    @Override
    public List<TimeEntry> createTimeEntries(List<CreateTimeEntryRequest> createTimeEntryRequests) {
        return timeEntryRepository.saveAll(createTimeEntryRequests.stream()
                .map(this::requestToTimeEntry).toList());
    }

    private TimeEntry requestToTimeEntry(CreateTimeEntryRequest createTimeEntryRequest) {
        return new TimeEntry(
                createTimeEntryRequest.user(),
                createTimeEntryRequest.project(),
                createTimeEntryRequest.task(),
                createTimeEntryRequest.description(),
                createTimeEntryRequest.isBillable(),
                createTimeEntryRequest.startTime(),
                createTimeEntryRequest.endTime());
    }

    @Override
    public TimeEntry patchTimeEntry(long userId, long timeEntryId, PatchTimeEntryRequest patchTimeEntryRequest) {
        final TimeEntry timeEntry = getTimeEntry(userId, timeEntryId);
        applyPatch(timeEntry, patchTimeEntryRequest);

        return timeEntryRepository.save(timeEntry);
    }

    @Override
    @Transactional
    public List<TimeEntry> patchTimeEntries(long userId, PatchTimeEntriesRequest patchTimeEntriesRequest) {
        final List<Long> timeEntryIds = patchTimeEntriesRequest.timeEntries().stream()
                .map(TimeEntryPatch::timeEntryId)
                .distinct()
                .toList();
        final Map<Long, TimeEntry> ownedTimeEntriesById = timeEntryRepository
                .findAllByIdInAndUserId(timeEntryIds, userId).stream()
                .collect(Collectors.toMap(TimeEntry::getId, Function.identity()));
        final List<Long> missingTimeEntryIds = timeEntryIds.stream()
                .filter(id -> !ownedTimeEntriesById.containsKey(id))
                .toList();

        if (!missingTimeEntryIds.isEmpty()) {
            throw new TimeEntriesNotFoundException(missingTimeEntryIds);
        }
        final List<TimeEntry> patchedTimeEntries = patchTimeEntriesRequest.timeEntries().stream()
                .map(timeEntryPatch -> {
                    final TimeEntry timeEntry = ownedTimeEntriesById.get(timeEntryPatch.timeEntryId());
                    applyPatch(timeEntry, timeEntryPatch.patch());

                    return timeEntry;
                })
                .toList();

        return timeEntryRepository.saveAll(patchedTimeEntries);
    }

    private void applyPatch(TimeEntry timeEntry, PatchTimeEntryRequest patchTimeEntryRequest) {
        patchTimeEntryRequest.project().ifPresent(timeEntry::setProject);
        patchTimeEntryRequest.task().ifPresent(timeEntry::setTask);
        patchTimeEntryRequest.description().ifPresent(timeEntry::setDescription);
        patchTimeEntryRequest.isBillable().ifPresent(timeEntry::setBillable);
        patchTimeEntryRequest.startTime().ifPresent(timeEntry::setStartTime);
        patchTimeEntryRequest.endTime().ifPresent(timeEntry::setEndTime);
    }

    @Override
    public void deleteTimeEntry(long userId, long timeEntryId) {
        getTimeEntry(userId, timeEntryId);

        timeEntryRepository.deleteById(timeEntryId);
    }

    @Override
    public void deleteTimeEntries(long userId, DeleteTimeEntriesRequest deleteTimeEntriesRequest) {
        final List<Long> timeEntryIds = deleteTimeEntriesRequest.timeEntryIds().stream().distinct().toList();
        final Set<Long> ownedTimeEntryIds = timeEntryRepository.findAllByIdInAndUserId(timeEntryIds, userId).stream()
                .map(TimeEntry::getId)
                .collect(Collectors.toSet());
        final List<Long> missingTimeEntryIds = timeEntryIds.stream()
                .filter(id -> !ownedTimeEntryIds.contains(id))
                .toList();

        if (!missingTimeEntryIds.isEmpty()) {
            throw new TimeEntriesNotFoundException(missingTimeEntryIds);
        }

        timeEntryRepository.deleteAllById(timeEntryIds);
    }
}
