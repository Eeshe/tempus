package me.eeshe.tempus.dto;

import java.util.List;

import org.springframework.web.multipart.MultipartFile;

public record ImportCSVFilesRequestDTO(
        List<MultipartFile> files) {
}
