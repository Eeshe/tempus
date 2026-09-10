package me.eeshe.tempus.service.impl;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import javax.sql.DataSource;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import me.eeshe.tempus.service.DatabaseMetaService;
import me.eeshe.tempus.service.SyncService;

@Service
public class SyncServiceImpl implements SyncService {
    private static final Path SYNC_DIRECTORY = Path.of("sync");

    private final JdbcTemplate jdbcTemplate;
    private final DataSource dataSource;
    private final DatabaseMetaService databaseMetaService;

    public SyncServiceImpl(JdbcTemplate jdbcTemplate, DataSource dataSource, DatabaseMetaService databaseMetaService) {
        this.jdbcTemplate = jdbcTemplate;
        this.dataSource = dataSource;
        this.databaseMetaService = databaseMetaService;
    }

    @Override
    public void exportSnapshot() {
        databaseMetaService.updateCurrentSnapshotTime();
        try {
            Files.createDirectories(SYNC_DIRECTORY);

            final Path tempSnapshotFile = SYNC_DIRECTORY.resolve("snapshot.db.tmp");
            jdbcTemplate.execute("VACUUM INTO '" + tempSnapshotFile + "'");

            final Path snapshotFile = SYNC_DIRECTORY.resolve("snapshot.db");
            Files.move(tempSnapshotFile, snapshotFile, StandardCopyOption.REPLACE_EXISTING);
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Export failed: " + e);
        }
    }

    @Override
    public void importSnapshot() {
        final Path snapshotFile = SYNC_DIRECTORY.resolve("snapshot.db");
        if (!snapshotFile.toFile().exists()) {
            return;
        }
        final LocalDateTime localSnapshotTime = databaseMetaService.getCurrentSnapshotTime();
        try (Connection connection = dataSource.getConnection()) {
            connection.setAutoCommit(false);

            attachSnapshot(connection, snapshotFile);
            if (isNewerSnapshot(connection, localSnapshotTime)) {
                replaceTables(connection);
            }
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

    private boolean isNewerSnapshot(Connection connection, LocalDateTime localSnapshotTime) throws SQLException {
        if (localSnapshotTime == null) {
            return true;
        }
        try (Statement statement = connection.createStatement()) {
            final ResultSet resultSet = statement
                    .executeQuery("SELECT current_snapshot_time FROM src.database_meta");
            if (!resultSet.next()) {
                // Snapshot doesn't have database meta
                return false;
            }
            final LocalDateTime snapshotTime = LocalDateTime.parse(resultSet.getString(1));

            return snapshotTime.isAfter(localSnapshotTime);
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
