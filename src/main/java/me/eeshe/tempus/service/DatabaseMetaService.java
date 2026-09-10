package me.eeshe.tempus.service;

import java.nio.file.Path;
import java.time.Instant;
import java.util.Optional;

public interface DatabaseMetaService {
    Path SNAPSHOT_DIRECTORY = Path.of("sync");
    Path SNAPSHOT_FILE = SNAPSHOT_DIRECTORY.resolve("snapshot.db");

    void initializeDatabaseMeta();

    Optional<Instant> getLocalSnapshotTime();

    Optional<Instant> getRemoteSnapshotTime();

    void updateCurrentSnapshotTime();

    void updateCurrentSnapshotTime(Instant time);

    boolean isRemoteSnapshotNewer();
}
