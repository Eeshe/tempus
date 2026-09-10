package me.eeshe.tempus.config;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import me.eeshe.tempus.service.SyncService;

@Component
@Order(2)
public class AutoImportRunner implements ApplicationRunner {
    private final SyncService syncService;

    public AutoImportRunner(SyncService syncService) {
        this.syncService = syncService;
    }

    @Override
    public void run(ApplicationArguments args) throws Exception {
        syncService.importSnapshot();
    }
}
