package me.eeshe.tempus.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import me.eeshe.tempus.dto.SyncDataDTO;
import me.eeshe.tempus.mapper.SyncDataMapper;
import me.eeshe.tempus.model.SyncData;
import me.eeshe.tempus.service.SyncService;

@RestController
@RequestMapping(path = "api/v1/sync")
public class SyncController {
    private final SyncService syncService;
    private final SyncDataMapper syncDataMapper;

    public SyncController(SyncService syncService, SyncDataMapper syncDataMapper) {
        this.syncService = syncService;
        this.syncDataMapper = syncDataMapper;
    }

    @GetMapping()
    public ResponseEntity<SyncDataDTO> getSyncData() {
        final SyncData syncData = syncService.getSyncData();
        final SyncDataDTO syncDataDTO = syncDataMapper.toDTO(syncData);

        return ResponseEntity.ok(syncDataDTO);
    }

    @PostMapping(path = "/export")
    public ResponseEntity<Void> exportSnapshot() {
        syncService.exportSnapshot();

        return ResponseEntity.ok().build();
    }

    @PostMapping(path = "/import")
    public ResponseEntity<Void> importSnapshot() {
        syncService.importSnapshot();

        return ResponseEntity.ok().build();
    }
}
