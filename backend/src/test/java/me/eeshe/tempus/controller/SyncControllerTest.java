package me.eeshe.tempus.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.time.Instant;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import jakarta.servlet.ServletException;
import me.eeshe.tempus.dto.SyncDataDTO;
import me.eeshe.tempus.mapper.SyncDataMapper;
import me.eeshe.tempus.model.SyncData;
import me.eeshe.tempus.service.SyncService;
import me.eeshe.tempus.support.ControllerTestBase;

@WebMvcTest(SyncController.class)
public class SyncControllerTest extends ControllerTestBase {
    private static final Instant LOCAL_SNAPSHOT_TIME = Instant.parse("2026-01-01T10:00:00Z");
    private static final Instant REMOTE_SNAPSHOT_TIME = Instant.parse("2026-01-02T10:00:00Z");

    @MockitoBean
    private SyncService syncService;

    @MockitoBean
    private SyncDataMapper syncDataMapper;

    @Nested
    class GetSyncData {
        private static final String URL = "/api/v1/sync";

        @Test
        void returnsSyncData() {
            final SyncData syncData = createSyncData();

            when(syncService.getSyncData()).thenReturn(syncData);
            when(syncDataMapper.toDTO(syncData)).thenReturn(createSyncDataDTO());

            assertThat(mockMvc.get().uri(URL).with(createPrincipal(USER_ID)))
                    .hasStatus(200)
                    .bodyJson()
                    .isEqualTo(createSyncDataDTOJson());

            verify(syncService).getSyncData();
            verify(syncDataMapper).toDTO(syncData);
        }

        @Test
        void returnsMissingSyncData() {
            final SyncData syncData = new SyncData(null, null);

            when(syncService.getSyncData()).thenReturn(syncData);
            when(syncDataMapper.toDTO(syncData)).thenReturn(new SyncDataDTO(null, null));

            assertThat(mockMvc.get().uri(URL).with(createPrincipal(USER_ID)))
                    .hasStatus(200)
                    .bodyJson()
                    .isEqualTo("""
                            {
                                "localSnapshotTime": null,
                                "remoteSnapshotTime": null
                            }""");

            verify(syncService).getSyncData();
        }

        @Test
        void rejectsUnauthenticatedRequest() {
            assertThat(mockMvc.get().uri(URL)).hasStatus(401);

            verifyNoInteractions(syncService, syncDataMapper);
        }
    }

    @Nested
    class ExportSnapshot {
        private static final String URL = "/api/v1/sync/export";

        @Test
        void exportsSnapshot() {
            assertThat(mockMvc.post().uri(URL).with(createPrincipalWithCsrf(USER_ID)))
                    .hasStatus(200);

            verify(syncService).exportSnapshot();
        }

        @Test
        void rejectsSnapshotFailure() {
            doThrow(new RuntimeException("Export failed")).when(syncService).exportSnapshot();

            assertThat(mockMvc.post().uri(URL).with(createPrincipalWithCsrf(USER_ID)))
                    .failure()
                    .isInstanceOf(ServletException.class)
                    .hasRootCauseInstanceOf(RuntimeException.class)
                    .hasRootCauseMessage("Export failed");

            verify(syncService).exportSnapshot();
        }

        @Test
        void rejectsUnauthenticatedRequest() {
            assertThat(mockMvc.post().uri(URL).with(createCsrf())).hasStatus(401);

            verifyNoInteractions(syncService);
        }
    }

    @Nested
    class ImportSnapshot {
        private static final String URL = "/api/v1/sync/import";

        @Test
        void importsSnapshot() {
            assertThat(mockMvc.post().uri(URL).with(createPrincipalWithCsrf(USER_ID)))
                    .hasStatus(200);

            verify(syncService).importSnapshot();
        }

        @Test
        void rejectsSnapshotFailure() {
            doThrow(new RuntimeException("Import failed")).when(syncService).importSnapshot();

            assertThat(mockMvc.post().uri(URL).with(createPrincipalWithCsrf(USER_ID)))
                    .failure()
                    .isInstanceOf(ServletException.class)
                    .hasRootCauseInstanceOf(RuntimeException.class)
                    .hasRootCauseMessage("Import failed");

            verify(syncService).importSnapshot();
        }

        @Test
        void rejectsUnauthenticatedRequest() {
            assertThat(mockMvc.post().uri(URL).with(createCsrf())).hasStatus(401);

            verifyNoInteractions(syncService);
        }
    }

    private static SyncData createSyncData() {
        return new SyncData(LOCAL_SNAPSHOT_TIME, REMOTE_SNAPSHOT_TIME);
    }

    private static SyncDataDTO createSyncDataDTO() {
        return new SyncDataDTO(LOCAL_SNAPSHOT_TIME, REMOTE_SNAPSHOT_TIME);
    }

    private static String createSyncDataDTOJson() {
        return """
                {
                    "localSnapshotTime": "%s",
                    "remoteSnapshotTime": "%s"
                }""".formatted(LOCAL_SNAPSHOT_TIME, REMOTE_SNAPSHOT_TIME);
    }
}
