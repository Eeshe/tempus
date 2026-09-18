package me.eeshe.tempus.mapper;

import me.eeshe.tempus.dto.CreateTimeEntryRequestDTO;
import me.eeshe.tempus.dto.PatchTimeEntryRequestDTO;
import me.eeshe.tempus.dto.TimeEntryDTO;
import me.eeshe.tempus.entity.TimeEntry;
import me.eeshe.tempus.request.CreateTimeEntryRequest;
import me.eeshe.tempus.request.PatchTimeEntryRequest;

public interface TimeEntryMapper {

    TimeEntryDTO toDTO(TimeEntry timeEntry);

    CreateTimeEntryRequest fromDTO(CreateTimeEntryRequestDTO createTimeEntryRequestDTO, long userId);

    PatchTimeEntryRequest fromDTO(PatchTimeEntryRequestDTO patchTimeEntryRequestDTO, long userId);
}
