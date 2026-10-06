package me.eeshe.tempus.controller;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import me.eeshe.tempus.dto.ImportCSVFilesRequestDTO;
import me.eeshe.tempus.dto.ImportResultDTO;
import me.eeshe.tempus.mapper.ImportMapper;
import me.eeshe.tempus.model.ImportResult;
import me.eeshe.tempus.request.ImportCSVFilesRequest;
import me.eeshe.tempus.security.UserDetailsImpl;
import me.eeshe.tempus.service.ImportService;

@RestController
@RequestMapping(path = "/api/v1/import")
public class ImportController {
    private final ImportService importService;
    private final ImportMapper importMapper;

    public ImportController(ImportService importService, ImportMapper importMapper) {
        this.importService = importService;
        this.importMapper = importMapper;
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ImportResultDTO> importCsvFiles(
            @Valid ImportCSVFilesRequestDTO importCSVFilesRequestDTO,
            @AuthenticationPrincipal UserDetailsImpl userDetails) {
        final ImportCSVFilesRequest importCSVFilesRequest = importMapper.fromDTO(importCSVFilesRequestDTO);
        final ImportResult importResult = importService.importCSVFiles(userDetails.getId(), importCSVFilesRequest);

        return ResponseEntity.ok(importMapper.toDTO(importResult));
    }
}
