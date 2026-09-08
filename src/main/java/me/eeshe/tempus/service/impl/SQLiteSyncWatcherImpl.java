package me.eeshe.tempus.service.impl;

import java.io.File;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;

import com.zaxxer.hikari.HikariDataSource;

import me.eeshe.tempus.service.SQLiteSyncWatcher;

public class SQLiteSyncWatcherImpl implements SQLiteSyncWatcher {
    private static final Logger LOGGER = LoggerFactory.getLogger(SQLiteSyncWatcherImpl.class);

    @Value("${spring.datasource.url}")
    private String datasourceUrl;

    private final HikariDataSource dataSource;
    private long lastKnownModified = 0;
    private File sqliteFile;

    public SQLiteSyncWatcherImpl(HikariDataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public void init() {
        final String sqliteFilePath = datasourceUrl.replace("jdbc:sqlite", "")
                .split("\\?")[0];

        this.sqliteFile = new File(sqliteFilePath);
        this.lastKnownModified = sqliteFile.lastModified();
    }

    @Override
    public void checkForSQLiteFileChanges() {
    }
}
