package me.eeshe.tempus.dto;

import java.time.Instant;

public record SyncDataDTO(
        Instant localSnapshotTime,
        Instant remoteSnapshotTime) {
}
