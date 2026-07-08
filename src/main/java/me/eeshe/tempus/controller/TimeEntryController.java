package me.eeshe.tempus.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import me.eeshe.tempus.entity.TimeEntry;
import me.eeshe.tempus.entity.dto.TimeEntryDTO;
import me.eeshe.tempus.mapper.TimeEntryMapper;
import me.eeshe.tempus.service.TimeEntryService;

@RestController
@RequestMapping(path = "api/v1/time-entries")
public class TimeEntryController {
    private final TimeEntryService timeEntryService;
    private final TimeEntryMapper timeEntryMapper;

    public TimeEntryController(TimeEntryService timeEntryService, TimeEntryMapper timeEntryMapper) {
        this.timeEntryService = timeEntryService;
        this.timeEntryMapper = timeEntryMapper;
    }

    @GetMapping
    public ResponseEntity<List<TimeEntryDTO>> getAll() {
        final List<TimeEntry> timeEntries = timeEntryService.getAllTimeEntries();
        final List<TimeEntryDTO> timeEntryDTOs = timeEntries.stream().map(timeEntryMapper::toDto).toList();

        return ResponseEntity.ok(timeEntryDTOs);
    }

    @PostMapping
    public ResponseEntity<TimeEntryDTO> create(TimeEntryDTO timeEntryDTO) {
        final TimeEntry timeEntry = timeEntryMapper.fromDto(timeEntryDTO);

        timeEntryService.saveTimeEntry(timeEntry);

        return new ResponseEntity<>(timeEntryDTO, HttpStatus.CREATED);
    }

    @PutMapping
    public ResponseEntity<TimeEntryDTO> update(TimeEntryDTO timeEntryDTO) {
        final TimeEntry timeEntry = timeEntryMapper.fromDto(timeEntryDTO);

        timeEntryService.updateTimeEntry(timeEntry);

        return ResponseEntity.ok(timeEntryDTO);
    }
}
