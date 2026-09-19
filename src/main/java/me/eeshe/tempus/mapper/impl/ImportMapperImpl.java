package me.eeshe.tempus.mapper.impl;

import org.springframework.stereotype.Component;

import me.eeshe.tempus.dto.ImportCSVFilesRequestDTO;
import me.eeshe.tempus.mapper.ImportMapper;
import me.eeshe.tempus.request.ImportCSVFilesRequest;

@Component
public class ImportMapperImpl implements ImportMapper {

    @Override
    public ImportCSVFilesRequest fromDTO(ImportCSVFilesRequestDTO importCSVFilesRequestDTO) {
        return new ImportCSVFilesRequest(importCSVFilesRequestDTO.files());
    }
}
