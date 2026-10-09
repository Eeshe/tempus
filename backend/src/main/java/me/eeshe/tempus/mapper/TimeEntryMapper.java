package me.eeshe.tempus.mapper;

import me.eeshe.tempus.dto.CreateTimeEntryRequestDTO;
import me.eeshe.tempus.dto.DeleteTimeEntriesRequestDTO;
import me.eeshe.tempus.dto.PatchTimeEntriesRequestDTO;
import me.eeshe.tempus.dto.PatchTimeEntryRequestDTO;
import me.eeshe.tempus.dto.TimeEntryDTO;
import me.eeshe.tempus.entity.TimeEntry;
import me.eeshe.tempus.model.CSVTimeEntry;
import me.eeshe.tempus.request.CreateTimeEntryRequest;
import me.eeshe.tempus.request.DeleteTimeEntriesRequest;
import me.eeshe.tempus.request.PatchTimeEntriesRequest;
import me.eeshe.tempus.request.PatchTimeEntryRequest;

public interface TimeEntryMapper {

    TimeEntryDTO toDTO(TimeEntry timeEntry);

    CreateTimeEntryRequest fromDTO(CreateTimeEntryRequestDTO createTimeEntryRequestDTO, long userId);

    CreateTimeEntryRequest fromCSVTimeEntry(CSVTimeEntry csvTimeEntry, long userId);

    PatchTimeEntryRequest fromDTO(PatchTimeEntryRequestDTO patchTimeEntryRequestDTO, long userId);

    DeleteTimeEntriesRequest fromDTO(DeleteTimeEntriesRequestDTO deleteTimeEntriesRequestDTO);

    PatchTimeEntriesRequest fromDTO(PatchTimeEntriesRequestDTO patchTimeEntriesRequestDTO, long userId);
}
