package me.eeshe.tempus.service;

import java.util.List;

import me.eeshe.tempus.model.SyncData;

public interface SyncService {
    public static final List<String> SKIPEED_TABLES = List.of(
            "SPRING_SESSION",
            "SPRING_SESSION_ATTRIBUTES",
            "flyway_schema_history");

    SyncData getSyncData();

    void exportSnapshot();

    void importSnapshot();
}
