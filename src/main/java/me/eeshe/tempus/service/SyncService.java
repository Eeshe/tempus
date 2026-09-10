package me.eeshe.tempus.service;

import me.eeshe.tempus.model.SyncData;

public interface SyncService {

    SyncData getSyncData();

    void exportSnapshot();

    void importSnapshot();
}
