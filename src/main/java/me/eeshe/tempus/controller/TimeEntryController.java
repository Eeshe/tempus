package me.eeshe.tempus.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import me.eeshe.tempus.dto.CreateTimeEntryRequestDTO;
import me.eeshe.tempus.dto.PatchTimeEntryRequestDTO;
import me.eeshe.tempus.dto.TimeEntryDTO;
import me.eeshe.tempus.entity.TimeEntry;
import me.eeshe.tempus.mapper.TimeEntryMapper;
import me.eeshe.tempus.request.CreateTimeEntryRequest;
import me.eeshe.tempus.request.PatchTimeEntryRequest;
import me.eeshe.tempus.security.UserDetailsImpl;
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
    public ResponseEntity<List<TimeEntryDTO>> listTimeEntries() {
        final List<TimeEntry> timeEntries = timeEntryService.listTimeEntries();
        final List<TimeEntryDTO> timeEntryDTOs = timeEntries.stream().map(timeEntryMapper::toDTO).toList();

        return ResponseEntity.ok(timeEntryDTOs);
    }

    @GetMapping(path = "/{timeEntryId}")
    public ResponseEntity<TimeEntryDTO> getTimeEntry(@PathVariable long timeEntryId) {
        final TimeEntry timeEntry = timeEntryService.getTimeEntry(timeEntryId);
        final TimeEntryDTO timeEntryDTO = timeEntryMapper.toDTO(timeEntry);

        return ResponseEntity.ok(timeEntryDTO);
    }

    @PostMapping
    public ResponseEntity<TimeEntryDTO> createTimeEntry(
            @Valid @RequestBody CreateTimeEntryRequestDTO createTimeEntryRequestDTO,
            @AuthenticationPrincipal UserDetailsImpl userDetails) {
        final CreateTimeEntryRequest createTimeEntryRequest = timeEntryMapper.fromDTO(
                createTimeEntryRequestDTO,
                userDetails.getId());
        final TimeEntry createdTimeEntry = timeEntryService.createTimeEntry(createTimeEntryRequest);
        final TimeEntryDTO createdTimeEntryDTO = timeEntryMapper.toDTO(createdTimeEntry);

        return new ResponseEntity<>(createdTimeEntryDTO, HttpStatus.CREATED);
    }

    @PatchMapping(path = "/{timeEntryId}")
    public ResponseEntity<TimeEntryDTO> patchTimeEntry(
            @PathVariable long timeEntryId,
            @Valid @RequestBody PatchTimeEntryRequestDTO patchTimeEntryRequestDTO) {
        final PatchTimeEntryRequest patchTimeEntryRequest = timeEntryMapper.fromDTO(patchTimeEntryRequestDTO);
        final TimeEntry patchedTimeEntry = timeEntryService.patchTimeEntry(timeEntryId, patchTimeEntryRequest);
        final TimeEntryDTO patchedTimeEntryDTO = timeEntryMapper.toDTO(patchedTimeEntry);

        return ResponseEntity.ok(patchedTimeEntryDTO);
    }

    @DeleteMapping(path = "/{timeEntryId}")
    public ResponseEntity<TimeEntryDTO> deleteTimeEntry(@PathVariable long timeEntryId) {
        timeEntryService.deleteTimeEntry(timeEntryId);

        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}
