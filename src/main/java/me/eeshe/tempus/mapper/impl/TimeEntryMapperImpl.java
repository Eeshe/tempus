package me.eeshe.tempus.mapper.impl;

import me.eeshe.tempus.dto.TimeEntryDTO;
import me.eeshe.tempus.entity.TimeEntry;
import me.eeshe.tempus.mapper.TimeEntryMapper;

public class TimeEntryMapperImpl implements TimeEntryMapper {

    @Override
    public TimeEntryDTO toDto(TimeEntry timeEntry) {
        return new TimeEntryDTO(
                timeEntry.getId(),
                timeEntry.getGroup().getId(),
                timeEntry.getUser().getId(),
                timeEntry.getProject().getId(),
                timeEntry.getTask().getId(),
                timeEntry.getDescription(),
                timeEntry.isBillable());
    }
}
