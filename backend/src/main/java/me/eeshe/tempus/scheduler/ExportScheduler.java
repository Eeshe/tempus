package me.eeshe.tempus.scheduler;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import me.eeshe.tempus.service.DatabaseMetaService;
import me.eeshe.tempus.service.SyncService;

@Component
public class ExportScheduler {
    private final DatabaseMetaService databaseMetaService;
    private final SyncService syncService;

    public ExportScheduler(DatabaseMetaService databaseMetaService, SyncService syncService) {
        this.databaseMetaService = databaseMetaService;
        this.syncService = syncService;
    }

    @Scheduled(fixedRate = 300000, initialDelay = 300000)
    public void exportSnapshot() {
        if (databaseMetaService.isRemoteSnapshotNewer()) {
            return;
        }
        syncService.exportSnapshot();
    }
}
