package me.eeshe.tempus.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import me.eeshe.tempus.service.SyncService;

@RestController
@RequestMapping(path = "api/v1/sync")
public class SyncController {
    private final SyncService syncService;

    public SyncController(SyncService syncService) {
        this.syncService = syncService;
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
