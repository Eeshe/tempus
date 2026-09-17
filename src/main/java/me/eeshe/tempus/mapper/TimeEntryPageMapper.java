package me.eeshe.tempus.mapper;

import me.eeshe.tempus.dto.TimeEntryPageDTO;
import me.eeshe.tempus.model.TimeEntryPage;

public interface TimeEntryPageMapper {

    TimeEntryPageDTO toDTO(TimeEntryPage timeEntryPage);
}
