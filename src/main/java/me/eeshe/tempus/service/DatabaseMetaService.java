package me.eeshe.tempus.service;

import java.nio.file.Path;
import java.time.LocalDateTime;

public interface DatabaseMetaService {
    Path SNAPSHOT_DIRECTORY = Path.of("sync");
    Path SNAPSHOT_FILE = SNAPSHOT_DIRECTORY.resolve("snapshot.db");

    void initializeDatabaseMeta();

    LocalDateTime getLocalSnapshotTime();

    LocalDateTime getRemoteSnapshotTime();

    void updateCurrentSnapshotTime();

    void updateCurrentSnapshotTime(LocalDateTime time);

    boolean isRemoteSnapshotNewer();
}
