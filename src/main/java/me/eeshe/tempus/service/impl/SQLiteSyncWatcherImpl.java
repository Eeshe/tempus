package me.eeshe.tempus.service.impl;

import java.io.File;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.zaxxer.hikari.HikariDataSource;

import jakarta.annotation.PostConstruct;
import me.eeshe.tempus.service.SQLiteSyncWatcher;

@Service
public class SQLiteSyncWatcherImpl implements SQLiteSyncWatcher {

    @Value("${spring.datasource.url}")
    private String datasourceUrl;

    private final HikariDataSource dataSource;
    private long lastKnownModified = 0;
    private File sqliteFile;

    public SQLiteSyncWatcherImpl(HikariDataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    @PostConstruct
    public void init() {
        final String sqliteFilePath = datasourceUrl.replace("jdbc:sqlite", "")
                .split("\\?")[0];

        this.sqliteFile = new File(sqliteFilePath);
        this.lastKnownModified = sqliteFile.lastModified();
    }

    @Override
    @Scheduled(fixedDelay = 5000)
    public void checkForSQLiteFileChanges() {
        if (!sqliteFile.exists()) {
            return;
        }
        final long currentLastModified = sqliteFile.lastModified();
        if (this.lastKnownModified >= currentLastModified) {
            return;
        }
        this.lastKnownModified = currentLastModified;
        closeCurrentSQLiteConnection();
    }

    private void closeCurrentSQLiteConnection() {
        dataSource.getHikariPoolMXBean().softEvictConnections();
    }
}
