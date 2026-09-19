package me.eeshe.tempus.service;

import me.eeshe.tempus.request.ImportCSVFilesRequest;

public interface ImportService {

    void importCSVFiles(long userId, ImportCSVFilesRequest importCSVFilesRequest);
}
