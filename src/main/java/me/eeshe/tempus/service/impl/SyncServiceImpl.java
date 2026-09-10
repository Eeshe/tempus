package me.eeshe.tempus.service.impl;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import javax.sql.DataSource;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import me.eeshe.tempus.model.SyncData;
import me.eeshe.tempus.service.DatabaseMetaService;
import me.eeshe.tempus.service.SyncService;

@Service
public class SyncServiceImpl implements SyncService {
    private final JdbcTemplate jdbcTemplate;
    private final DataSource dataSource;
    private final DatabaseMetaService databaseMetaService;

    public SyncServiceImpl(JdbcTemplate jdbcTemplate, DataSource dataSource, DatabaseMetaService databaseMetaService) {
        this.jdbcTemplate = jdbcTemplate;
        this.dataSource = dataSource;
        this.databaseMetaService = databaseMetaService;
    }

    @Override
    public SyncData getSyncData() {
        final Instant localSnapshotTime = databaseMetaService.getLocalSnapshotTime().orElse(null);
        final Instant remoteSnapshotTime = databaseMetaService.getRemoteSnapshotTime().orElse(null);

        return new SyncData(localSnapshotTime, remoteSnapshotTime);
    }

    @Override
    public void exportSnapshot() {
        if (databaseMetaService.isRemoteSnapshotNewer()) {
            return;
        }
        databaseMetaService.updateCurrentSnapshotTime();
        try {
            Files.createDirectories(DatabaseMetaService.SNAPSHOT_DIRECTORY);

            final Path tempSnapshotFile = DatabaseMetaService.SNAPSHOT_DIRECTORY.resolve("snapshot.db.tmp");
            jdbcTemplate.execute("VACUUM INTO '" + tempSnapshotFile + "'");

            Files.move(tempSnapshotFile, DatabaseMetaService.SNAPSHOT_FILE, StandardCopyOption.REPLACE_EXISTING);
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Export failed: " + e);
        }
    }

    @Override
    public void importSnapshot() {
        if (!databaseMetaService.isRemoteSnapshotNewer()) {
            return;
        }
        try (Connection connection = dataSource.getConnection()) {
            connection.setAutoCommit(false);

            attachSnapshot(connection, DatabaseMetaService.SNAPSHOT_FILE);
            replaceTables(connection);
            detachSnapshot(connection);
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    private void attachSnapshot(Connection connection, Path snapshotFile) throws SQLException {
        try (Statement statement = connection.createStatement()) {
            statement.execute("PRAGMA foreign_keys = OFF");
            statement.execute("ATTACH DATABASE '" + snapshotFile + "' AS src");
        }
    }

    private void replaceTables(Connection connection) throws SQLException {
        final List<String> tables = new ArrayList<>();
        try (Statement statement = connection.createStatement();
                final ResultSet resultSet = statement.executeQuery(
                        "SELECT name FROM src.sqlite_master WHERE type='table' AND name NOT LIKE 'sqlite_%'")) {
            while (resultSet.next()) {
                tables.add(resultSet.getString("name"));
            }
        }
        try (Statement statement = connection.createStatement()) {
            for (String table : tables) {
                statement.execute("DELETE FROM main." + table);
                statement.execute("INSERT INTO main." + table + " SELECT * FROM src." + table);
            }
        }
    }

    private void detachSnapshot(Connection connection) throws SQLException {
        connection.commit();
        try (Statement statement = connection.createStatement()) {
            statement.execute("DETACH src");
            statement.execute("PRAGMA foreign_keys = ON");
        }
    }
}
