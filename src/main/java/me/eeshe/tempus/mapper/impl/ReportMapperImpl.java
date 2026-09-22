package me.eeshe.tempus.mapper.impl;

import java.util.List;

import org.springframework.stereotype.Component;

import me.eeshe.tempus.dto.ClientReportEntryDTO;
import me.eeshe.tempus.dto.ProjectReportEntryDTO;
import me.eeshe.tempus.dto.ReportDTO;
import me.eeshe.tempus.dto.ReportRequestDTO;
import me.eeshe.tempus.mapper.ClientMapper;
import me.eeshe.tempus.mapper.ProjectMapper;
import me.eeshe.tempus.mapper.ReportMapper;
import me.eeshe.tempus.model.ClientReportEntry;
import me.eeshe.tempus.model.ProjectReportEntry;
import me.eeshe.tempus.model.Report;
import me.eeshe.tempus.request.ReportRequest;

@Component
public class ReportMapperImpl implements ReportMapper {
    private final ProjectMapper projectMapper;
    private final ClientMapper clientMapper;

    public ReportMapperImpl(ProjectMapper projectMapper, ClientMapper clientMapper) {
        this.projectMapper = projectMapper;
        this.clientMapper = clientMapper;
    }

    @Override
    public ReportDTO<ProjectReportEntryDTO> toProjectDTO(Report<ProjectReportEntry> report) {
        final List<ProjectReportEntryDTO> projectReportEntryDTOs = report.getReportEntries().stream()
                .map(entry -> new ProjectReportEntryDTO(projectMapper.toDTO(entry.project()),
                        entry.trackedTimeMillis()))
                .toList();
        return new ReportDTO<>(
                report.getTotalTrackedTimeMillis(),
                report.getTotalBillableTrackedTimeMillis(),
                report.getTotalNonBillableTrackedTimeMillis(),
                report.getTotalAccumulatedPay(),
                projectReportEntryDTOs);
    }

    @Override
    public ReportDTO<ClientReportEntryDTO> toClientDTO(Report<ClientReportEntry> report) {
        final List<ClientReportEntryDTO> clientReportEntryDTOs = report.getReportEntries().stream()
                .map(entry -> new ClientReportEntryDTO(clientMapper.toDTO(entry.client()),
                        entry.trackedTimeMillis()))
                .toList();
        return new ReportDTO<>(
                report.getTotalTrackedTimeMillis(),
                report.getTotalBillableTrackedTimeMillis(),
                report.getTotalNonBillableTrackedTimeMillis(),
                report.getTotalAccumulatedPay(),
                clientReportEntryDTOs);
    }

    @Override
    public ReportRequest fromDTO(ReportRequestDTO reportRequestDTO, long userId) {
        return new ReportRequest(
                userId,
                reportRequestDTO.startDate(),
                reportRequestDTO.endDate(),
                reportRequestDTO.projectIds(),
                reportRequestDTO.taskIds(),
                reportRequestDTO.clientIds(),
                reportRequestDTO.descriptions(),
                reportRequestDTO.isBillable());
    }
}
