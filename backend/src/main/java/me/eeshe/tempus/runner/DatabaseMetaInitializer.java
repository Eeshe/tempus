package me.eeshe.tempus.runner;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import me.eeshe.tempus.service.DatabaseMetaService;

@Component
@Order(1)
public class DatabaseMetaInitializer implements ApplicationRunner {
    private final DatabaseMetaService databaseMetaService;

    public DatabaseMetaInitializer(DatabaseMetaService databaseMetaService) {
        this.databaseMetaService = databaseMetaService;
    }

    @Override
    public void run(ApplicationArguments args) throws Exception {
        databaseMetaService.initializeDatabaseMeta();
    }
}
