package me.eeshe.tempus.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import me.eeshe.tempus.dto.TimeEntryDTO;
import me.eeshe.tempus.entity.TimeEntry;
import me.eeshe.tempus.mapper.TimeEntryMapper;
import me.eeshe.tempus.service.TimeEntryService;

@RequestMapping(path = "api/v1/time-entries")
public class TimeEntryController {
    private final TimeEntryService timeEntryService;
    private final TimeEntryMapper timeEntryMapper;

    public TimeEntryController(TimeEntryService timeEntryService, TimeEntryMapper timeEntryMapper) {
        this.timeEntryService = timeEntryService;
        this.timeEntryMapper = timeEntryMapper;
    }

    @GetMapping
    public ResponseEntity<List<TimeEntryDTO>> list() {
        final List<TimeEntry> timeEntries = timeEntryService.getAllTimeEntries();
        final List<TimeEntryDTO> timeEntryDTOs = timeEntries.stream().map(timeEntryMapper::toDto).toList();

        return ResponseEntity.ok(timeEntryDTOs);
    }
}
