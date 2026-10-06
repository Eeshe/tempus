package me.eeshe.tempus.service.impl;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

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
        jdbcTemplate.execute("CREATE TABLE IF NOT EXISTS %s (%s INTEGER NOT NULL)"
                .formatted(META_TABLE, TIME_COLUMN));
    }

    @Override
    public Optional<Instant> getLocalSnapshotTime() {
        final List<Instant> times = jdbcTemplate.query("SELECT %s FROM %s".formatted(TIME_COLUMN, META_TABLE),
                (resultSet, rowNumber) -> Instant.ofEpochMilli(resultSet.getLong(TIME_COLUMN)));
        if (times.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(times.getFirst());
    }

    @Override
    public Optional<Instant> getRemoteSnapshotTime() {
        if (!SNAPSHOT_FILE.toFile().exists()) {
            return Optional.empty();
        }
        try (Connection connection = DriverManager.getConnection("jdbc:sqlite:" + SNAPSHOT_FILE);
                Statement statement = connection.createStatement();
                ResultSet resultSet = statement.executeQuery(
                        "SELECT current_snapshot_time FROM database_meta")) {
            return resultSet.next() ? Optional.of(Instant.ofEpochMilli(resultSet.getLong(1))) : Optional.empty();
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    @Override
    public void updateCurrentSnapshotTime() {
        updateCurrentSnapshotTime(Instant.now());
    }

    @Override
    public void updateCurrentSnapshotTime(Instant instant) {
        final long epochMilli = instant.toEpochMilli();
        final int updated = jdbcTemplate.update("UPDATE %s SET %s = ?".formatted(
                META_TABLE,
                TIME_COLUMN),
                epochMilli);
        if (updated != 0) {
            return;
        }
        jdbcTemplate.update("INSERT INTO %s (%s) VALUES (?)".formatted(
                META_TABLE,
                TIME_COLUMN),
                epochMilli);
    }

    @Override
    public boolean isRemoteSnapshotNewer() {
        final Instant remoteTime = getRemoteSnapshotTime().orElse(null);
        if (remoteTime == null) {
            return false;
        }
        final Instant localTime = getLocalSnapshotTime().orElse(null);
        if (localTime == null) {
            return true;
        }
        return remoteTime.isAfter(localTime);
    }
}
