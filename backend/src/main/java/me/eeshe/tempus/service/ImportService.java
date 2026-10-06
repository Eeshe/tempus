package me.eeshe.tempus.service;

import me.eeshe.tempus.model.ImportResult;
import me.eeshe.tempus.request.ImportCSVFilesRequest;

public interface ImportService {

    ImportResult importCSVFiles(long userId, ImportCSVFilesRequest importCSVFilesRequest);
}
