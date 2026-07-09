package me.eeshe.tempus.mapper;

import me.eeshe.tempus.dto.TimeEntryDTO;
import me.eeshe.tempus.entity.TimeEntry;

public interface TimeEntryMapper {

    TimeEntryDTO toDto(TimeEntry timeEntry);

    TimeEntry fromDto(TimeEntryDTO timeEntryDTO);
}
