package me.eeshe.tempus.runner;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import me.eeshe.tempus.service.SyncService;

@Component
@Order(2)
public class AutoSyncRunner implements ApplicationRunner {
    private final SyncService syncService;

    public AutoSyncRunner(SyncService syncService) {
        this.syncService = syncService;
    }

    @Override
    public void run(ApplicationArguments args) throws Exception {
        syncService.importSnapshot();
    }
}
