package me.eeshe.tempus.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;

@ExtendWith(MockitoExtension.class)
public class DatabaseMetaServiceImplTest {
    private static final Instant INSTANT = Instant.parse("2026-01-01T10:00:00Z");
    private static final Instant EARLIER_INSTANT = Instant.parse("2026-01-01T09:00:00Z");
    private static final Instant LATER_INSTANT = Instant.parse("2026-01-01T11:00:00Z");
    private static final long EPOCH_MILLI = INSTANT.toEpochMilli();

    private static final String CREATE_TABLE_SQL = "CREATE TABLE IF NOT EXISTS database_meta (current_snapshot_time INTEGER NOT NULL)";
    private static final String UPDATE_SQL = "UPDATE database_meta SET current_snapshot_time = ?";
    private static final String INSERT_SQL = "INSERT INTO database_meta (current_snapshot_time) VALUES (?)";

    @Mock
    private JdbcTemplate jdbcTemplate;

    private DatabaseMetaServiceImpl databaseMetaService;

    @BeforeEach
    void setUp() {
        databaseMetaService = spy(new DatabaseMetaServiceImpl(jdbcTemplate));
    }

    @Nested
    class InitializeDatabaseMeta {

        @Test
        void createsDatabaseMetaTable() {
            databaseMetaService.initializeDatabaseMeta();

            verify(jdbcTemplate).execute(CREATE_TABLE_SQL);
        }
    }

    @Nested
    class GetLocalSnapshotTime {

        @Test
        void returnsLocalSnapshotTimeWhenPresent() {
            when(jdbcTemplate.query(anyString(), ArgumentMatchers.<RowMapper<Instant>>any()))
                    .thenReturn(List.of(INSTANT));

            assertThat(databaseMetaService.getLocalSnapshotTime()).contains(INSTANT);
            verify(jdbcTemplate).query(anyString(), ArgumentMatchers.<RowMapper<Instant>>any());
        }

        @Test
        void returnsEmptyWhenNoLocalSnapshotTime() {
            when(jdbcTemplate.query(anyString(), ArgumentMatchers.<RowMapper<Instant>>any()))
                    .thenReturn(List.of());

            assertThat(databaseMetaService.getLocalSnapshotTime()).isEmpty();
        }
    }

    @Nested
    class UpdateCurrentSnapshotTime {

        @Test
        void updatesExistingSnapshotTime() {
            when(jdbcTemplate.update(UPDATE_SQL, EPOCH_MILLI)).thenReturn(1);

            databaseMetaService.updateCurrentSnapshotTime(INSTANT);

            verify(jdbcTemplate).update(UPDATE_SQL, EPOCH_MILLI);
            verify(jdbcTemplate, never()).update(eq(INSERT_SQL), anyLong());
        }

        @Test
        void insertsSnapshotTimeWhenMissing() {
            when(jdbcTemplate.update(UPDATE_SQL, EPOCH_MILLI)).thenReturn(0);

            databaseMetaService.updateCurrentSnapshotTime(INSTANT);

            verify(jdbcTemplate).update(INSERT_SQL, EPOCH_MILLI);
        }
    }

    @Nested
    class IsRemoteSnapshotNewer {

        @Test
        void returnsTrueWhenLocalSnapshotMissing() {
            doReturn(Optional.of(LATER_INSTANT)).when(databaseMetaService).getRemoteSnapshotTime();
            doReturn(Optional.empty()).when(databaseMetaService).getLocalSnapshotTime();

            assertThat(databaseMetaService.isRemoteSnapshotNewer()).isTrue();
        }

        @Test
        void returnsTrueWhenRemoteSnapshotNewer() {
            when(databaseMetaService.getRemoteSnapshotTime()).thenReturn(Optional.of(LATER_INSTANT));
            when(databaseMetaService.getLocalSnapshotTime()).thenReturn(Optional.of(INSTANT));

            assertThat(databaseMetaService.isRemoteSnapshotNewer()).isTrue();
        }

        @Test
        void returnsFalseWhenRemoteSnapshotOlder() {
            when(databaseMetaService.getRemoteSnapshotTime()).thenReturn(Optional.of(EARLIER_INSTANT));
            when(databaseMetaService.getLocalSnapshotTime()).thenReturn(Optional.of(INSTANT));

            assertThat(databaseMetaService.isRemoteSnapshotNewer()).isFalse();
        }

        @Test
        void returnsFalseWhenRemoteSnapshotEqual() {
            when(databaseMetaService.getRemoteSnapshotTime()).thenReturn(Optional.of(INSTANT));
            when(databaseMetaService.getLocalSnapshotTime()).thenReturn(Optional.of(INSTANT));

            assertThat(databaseMetaService.isRemoteSnapshotNewer()).isFalse();
        }
    }
}
