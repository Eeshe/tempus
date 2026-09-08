package me.eeshe.tempus.service;

public interface SQLiteSyncWatcher {

    void init();

    void checkForSQLiteFileChanges();
}
