package me.eeshe.tempus.mapper.impl;

import java.util.List;

import org.springframework.stereotype.Component;

import me.eeshe.tempus.dto.ProjectReportEntryDTO;
import me.eeshe.tempus.dto.ReportDTO;
import me.eeshe.tempus.dto.ReportRequestDTO;
import me.eeshe.tempus.mapper.ProjectMapper;
import me.eeshe.tempus.mapper.ReportMapper;
import me.eeshe.tempus.model.Report;
import me.eeshe.tempus.request.ReportRequest;

@Component
public class ReportMapperImpl implements ReportMapper {
    private final ProjectMapper projectMapper;

    public ReportMapperImpl(ProjectMapper projectMapper) {
        this.projectMapper = projectMapper;
    }

    @Override
    public ReportDTO toDTO(Report report) {
        final List<ProjectReportEntryDTO> projectReportEntryDTOs = report.getProjectReportEntries().stream()
                .map(entry -> new ProjectReportEntryDTO(projectMapper.toDTO(entry.project()),
                        entry.trackedTimeMillis()))
                .toList();
        return new ReportDTO(
                report.getTotalTrackedTimeMillis(),
                projectReportEntryDTOs);
    }

    @Override
    public ReportRequest fromDTO(ReportRequestDTO reportRequestDTO) {
        return new ReportRequest(
                reportRequestDTO.startDate(),
                reportRequestDTO.endDate(),
                reportRequestDTO.projectIds(),
                reportRequestDTO.descriptions(),
                reportRequestDTO.isBillable());
    }
}
