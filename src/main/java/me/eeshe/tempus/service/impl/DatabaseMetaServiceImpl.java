package me.eeshe.tempus.service.impl;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import me.eeshe.tempus.service.DatabaseMetaService;

@Service
public class DatabaseMetaServiceImpl implements DatabaseMetaService {
    private static final String META_TABLE = "database_meta";
    private static final String TIME_COLUMN = "current_snapshot_time";

    private final JdbcTemplate jdbcTemplate;

    public DatabaseMetaServiceImpl(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void initializeDatabaseMeta() {
        jdbcTemplate.execute("CREATE TABLE IF NOT EXISTS %s (%s TEXT NOT NULL)"
                .formatted(META_TABLE, TIME_COLUMN));
    }

    @Override
    public LocalDateTime getLocalSnapshotTime() {
        final List<LocalDateTime> times = jdbcTemplate.query("SELECT %s FROM %s".formatted(TIME_COLUMN, META_TABLE),
                (resultSet, rowNumber) -> LocalDateTime.parse(resultSet.getString(TIME_COLUMN)));
        if (times.isEmpty()) {
            return null;
        }
        return times.getFirst();
    }

    @Override
    public LocalDateTime getRemoteSnapshotTime() {
        if (!SNAPSHOT_FILE.toFile().exists()) {
            return null;
        }
        try (Connection connection = DriverManager.getConnection("jdbc:sqlite:" + SNAPSHOT_FILE);
                Statement statement = connection.createStatement();
                ResultSet resultSet = statement.executeQuery(
                        "SELECT current_snapshot_time FROM database_meta")) {
            return resultSet.next() ? LocalDateTime.parse(resultSet.getString(1)) : null;
        } catch (Exception e) {
            return null;
        }
    }

    @Override
    public void updateCurrentSnapshotTime() {
        updateCurrentSnapshotTime(LocalDateTime.now());
    }

    @Override
    public void updateCurrentSnapshotTime(LocalDateTime time) {
        final int updated = jdbcTemplate.update("UPDATE %s SET %s = ?".formatted(
                META_TABLE,
                TIME_COLUMN),
                time.toString());
        if (updated != 0) {
            return;
        }
        jdbcTemplate.update("INSERT INTO %s (%s) VALUES (?)".formatted(
                META_TABLE,
                TIME_COLUMN),
                time.toString());
    }

    @Override
    public boolean isRemoteSnapshotNewer() {
        final LocalDateTime remoteTime = getRemoteSnapshotTime();
        if (remoteTime == null) {
            return false;
        }
        final LocalDateTime localTime = getLocalSnapshotTime();
        if (localTime == null) {
            return true;
        }
        return remoteTime.isAfter(localTime);
    }
}
