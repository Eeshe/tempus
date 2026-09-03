package me.eeshe.tempus.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import me.eeshe.tempus.dto.ReportDTO;
import me.eeshe.tempus.dto.ReportRequestDTO;
import me.eeshe.tempus.mapper.ReportMapper;
import me.eeshe.tempus.model.Report;
import me.eeshe.tempus.request.ReportRequest;
import me.eeshe.tempus.service.ReportService;

@RestController
@RequestMapping(path = "api/v1/reports")
public class ReportController {
    private final ReportService reportService;
    private final ReportMapper reportMapper;

    public ReportController(ReportService reportService, ReportMapper reportMapper) {
        this.reportService = reportService;
        this.reportMapper = reportMapper;
    }

    @GetMapping
    public ResponseEntity<ReportDTO> getReport(
            @Valid @RequestBody ReportRequestDTO reportRequestDTO) {
        final ReportRequest reportRequest = reportMapper.fromDTO(reportRequestDTO);
        final Report report = reportService.generateReport(reportRequest);
        final ReportDTO reportDTO = reportMapper.toDTO(report);

        return ResponseEntity.ok(reportDTO);
    }
}
