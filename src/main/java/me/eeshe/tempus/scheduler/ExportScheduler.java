package me.eeshe.tempus.scheduler;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import me.eeshe.tempus.service.SyncService;

@Component
public class ExportScheduler {
    private final SyncService syncService;

    public ExportScheduler(SyncService syncService) {
        this.syncService = syncService;
    }

    @Scheduled(fixedRate = 300000, initialDelay = 300000)
    public void exportSnapshot() {
        syncService.exportSnapshot();
    }
}
