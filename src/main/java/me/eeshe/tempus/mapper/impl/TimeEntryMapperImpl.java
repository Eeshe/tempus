package me.eeshe.tempus.mapper.impl;

import org.springframework.stereotype.Component;

import me.eeshe.tempus.dto.TimeEntryDTO;
import me.eeshe.tempus.entity.TimeEntry;
import me.eeshe.tempus.mapper.TimeEntryMapper;

@Component
public class TimeEntryMapperImpl implements TimeEntryMapper {

    @Override
    public TimeEntryDTO toDto(TimeEntry timeEntry) {
        return new TimeEntryDTO(
                timeEntry.getId(),
                timeEntry.getGroupId(),
                timeEntry.getUserId(),
                timeEntry.getProjectId(),
                timeEntry.getTaskId(),
                timeEntry.getDescription(),
                timeEntry.isBillable());
    }

    @Override
    public TimeEntry fromDto(TimeEntryDTO timeEntryDTO) {
        return new TimeEntry(
                timeEntryDTO.id(),
                timeEntryDTO.groupId(),
                timeEntryDTO.userId(),
                timeEntryDTO.projectId(),
                timeEntryDTO.taskId(),
                timeEntryDTO.description(),
                timeEntryDTO.isBillable());
    }

}
