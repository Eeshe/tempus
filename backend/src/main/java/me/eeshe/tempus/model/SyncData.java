package me.eeshe.tempus.model;

import java.time.Instant;

public record SyncData(
        Instant localSnapshotTime,
        Instant remoteSnapshotTime) {
}
