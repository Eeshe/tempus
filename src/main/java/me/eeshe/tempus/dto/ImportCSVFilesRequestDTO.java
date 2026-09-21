package me.eeshe.tempus.dto;

import java.util.List;

import org.springframework.web.multipart.MultipartFile;

import jakarta.validation.constraints.NotEmpty;
import me.eeshe.tempus.common.validation.annotation.CsvFile;

public record ImportCSVFilesRequestDTO(
        @NotEmpty(message = ERROR_MESSAGE_NO_FILES) List<@CsvFile(message = ERROR_MESSAGE_NOT_CSV) MultipartFile> files) {
    private static final String ERROR_MESSAGE_NO_FILES = "At least one CSV file must be uploaded";
    private static final String ERROR_MESSAGE_NOT_CSV = "All uploaded files must be in CSV format";
}
