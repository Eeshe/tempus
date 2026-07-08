package me.eeshe.tempus.mapper;

import me.eeshe.tempus.entity.TimeEntry;
import me.eeshe.tempus.entity.dto.TimeEntryDTO;

public interface TimeEntryMapper {

    TimeEntryDTO toDto(TimeEntry timeEntry);

    TimeEntry fromDto(TimeEntryDTO timeEntryDTO);
}
