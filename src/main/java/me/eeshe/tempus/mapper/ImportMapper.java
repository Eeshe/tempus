package me.eeshe.tempus.mapper;

import me.eeshe.tempus.dto.ImportCSVFilesRequestDTO;
import me.eeshe.tempus.request.ImportCSVFilesRequest;

public interface ImportMapper {

    ImportCSVFilesRequest fromDTO(ImportCSVFilesRequestDTO importCSVFilesRequestDTO);
}
