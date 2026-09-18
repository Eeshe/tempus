package me.eeshe.tempus.controller;

import java.time.Instant;

import org.springframework.format.annotation.DateTimeFormat;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import me.eeshe.tempus.dto.CreateTimeEntryRequestDTO;
import me.eeshe.tempus.dto.PatchTimeEntryRequestDTO;
import me.eeshe.tempus.dto.TimeEntryDTO;
import me.eeshe.tempus.dto.TimeEntryPageDTO;
import me.eeshe.tempus.entity.TimeEntry;
import me.eeshe.tempus.mapper.TimeEntryMapper;
import me.eeshe.tempus.mapper.TimeEntryPageMapper;
import me.eeshe.tempus.model.TimeEntryPage;
import me.eeshe.tempus.request.CreateTimeEntryRequest;
import me.eeshe.tempus.request.PatchTimeEntryRequest;
import me.eeshe.tempus.security.UserDetailsImpl;
import me.eeshe.tempus.service.TimeEntryService;

@RestController
@RequestMapping(path = "api/v1/time-entries")
public class TimeEntryController {
    private final TimeEntryService timeEntryService;
    private final TimeEntryMapper timeEntryMapper;
    private final TimeEntryPageMapper timeEntryPageMapper;

    public TimeEntryController(
            TimeEntryService timeEntryService,
            TimeEntryMapper timeEntryMapper,
            TimeEntryPageMapper timeEntryPageMapper) {
        this.timeEntryService = timeEntryService;
        this.timeEntryMapper = timeEntryMapper;
        this.timeEntryPageMapper = timeEntryPageMapper;
    }

    @GetMapping
    public ResponseEntity<TimeEntryPageDTO> listTimeEntries(
            @RequestParam(name = "cursor", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant cursor,
            @RequestParam(name = "size", defaultValue = "50") int size,
            @AuthenticationPrincipal UserDetailsImpl userDetails) {
        final TimeEntryPage timeEntryPage = timeEntryService.listTimeEntries(
                userDetails.getId(),
                cursor,
                size);

        return ResponseEntity.ok(timeEntryPageMapper.toDTO(timeEntryPage));
    }

    @GetMapping(path = "/{timeEntryId}")
    public ResponseEntity<TimeEntryDTO> getTimeEntry(
            @PathVariable long timeEntryId,
            @AuthenticationPrincipal UserDetailsImpl userDetails) {
        final TimeEntry timeEntry = timeEntryService.getTimeEntry(userDetails.getId(), timeEntryId);
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
            @Valid @RequestBody PatchTimeEntryRequestDTO patchTimeEntryRequestDTO,
            @AuthenticationPrincipal UserDetailsImpl userDetails) {
        final PatchTimeEntryRequest patchTimeEntryRequest = timeEntryMapper.fromDTO(patchTimeEntryRequestDTO, userDetails.getId());
        final TimeEntry patchedTimeEntry = timeEntryService.patchTimeEntry(userDetails.getId(), timeEntryId,
                patchTimeEntryRequest);
        final TimeEntryDTO patchedTimeEntryDTO = timeEntryMapper.toDTO(patchedTimeEntry);

        return ResponseEntity.ok(patchedTimeEntryDTO);
    }

    @DeleteMapping(path = "/{timeEntryId}")
    public ResponseEntity<TimeEntryDTO> deleteTimeEntry(
            @PathVariable long timeEntryId,
            @AuthenticationPrincipal UserDetailsImpl userDetails) {
        timeEntryService.deleteTimeEntry(userDetails.getId(), timeEntryId);

        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}
