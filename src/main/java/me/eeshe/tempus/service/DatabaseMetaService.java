package me.eeshe.tempus.service;

import java.time.LocalDateTime;

public interface DatabaseMetaService {

    void initializeDatabaseMeta();

    LocalDateTime getCurrentSnapshotTime();

    void updateCurrentSnapshotTime();

    void updateCurrentSnapshotTime(LocalDateTime time);
}
