package me.eeshe.tempus.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import me.eeshe.tempus.dto.ClientReportEntryDTO;
import me.eeshe.tempus.dto.ProjectReportEntryDTO;
import me.eeshe.tempus.dto.ReportDTO;
import me.eeshe.tempus.dto.ReportRequestDTO;
import me.eeshe.tempus.mapper.ReportMapper;
import me.eeshe.tempus.model.ClientReportEntry;
import me.eeshe.tempus.model.ProjectReportEntry;
import me.eeshe.tempus.model.Report;
import me.eeshe.tempus.request.ReportRequest;
import me.eeshe.tempus.security.UserDetailsImpl;
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

    @PostMapping(path = "/projects")
    public ResponseEntity<ReportDTO<ProjectReportEntryDTO>> getProjectReport(
            @Valid @RequestBody ReportRequestDTO reportRequestDTO,
            @AuthenticationPrincipal UserDetailsImpl userDetails) {
        final ReportRequest reportRequest = reportMapper.fromDTO(reportRequestDTO, userDetails.getId());
        final Report<ProjectReportEntry> report = reportService.generateProjectReport(reportRequest);
        final ReportDTO<ProjectReportEntryDTO> reportDTO = reportMapper.toProjectDTO(report);

        return ResponseEntity.ok(reportDTO);
    }

    @PostMapping(path = "/clients")
    public ResponseEntity<ReportDTO<ClientReportEntryDTO>> getClientReport(
            @Valid @RequestBody ReportRequestDTO reportRequestDTO,
            @AuthenticationPrincipal UserDetailsImpl userDetails) {
        final ReportRequest reportRequest = reportMapper.fromDTO(reportRequestDTO, userDetails.getId());
        final Report<ClientReportEntry> report = reportService.generateClientReport(reportRequest);
        final ReportDTO<ClientReportEntryDTO> reportDTO = reportMapper.toClientDTO(report);

        return ResponseEntity.ok(reportDTO);
    }
}
