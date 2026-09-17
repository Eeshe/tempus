package me.eeshe.tempus.mapper.impl;

import org.springframework.stereotype.Component;

import me.eeshe.tempus.dto.TimeEntryPageDTO;
import me.eeshe.tempus.mapper.TimeEntryMapper;
import me.eeshe.tempus.mapper.TimeEntryPageMapper;
import me.eeshe.tempus.model.TimeEntryPage;

@Component
public class TimeEntryPageMapperImpl implements TimeEntryPageMapper {
    private final TimeEntryMapper timeEntryMapper;

    public TimeEntryPageMapperImpl(TimeEntryMapper timeEntryMapper) {
        this.timeEntryMapper = timeEntryMapper;
    }

    @Override
    public TimeEntryPageDTO toDTO(TimeEntryPage timeEntryPage) {
        return new TimeEntryPageDTO(
                timeEntryPage.content().stream().map(timeEntryMapper::toDTO).toList(),
                timeEntryPage.previousCursor(),
                timeEntryPage.currentCursor(),
                timeEntryPage.nextCursor(),
                timeEntryPage.page(),
                timeEntryPage.size(),
                timeEntryPage.totalElements(),
                timeEntryPage.totalPages(),
                timeEntryPage.first(),
                timeEntryPage.last());
    }
}
