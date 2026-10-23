package me.eeshe.tempus.mapper.impl;

import java.time.DateTimeException;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.List;

import org.springframework.stereotype.Component;

import me.eeshe.tempus.dto.ClientReportEntryDTO;
import me.eeshe.tempus.dto.ProjectReportEntryDTO;
import me.eeshe.tempus.dto.ReportDTO;
import me.eeshe.tempus.dto.ReportDayDTO;
import me.eeshe.tempus.dto.ReportRequestDTO;
import me.eeshe.tempus.exception.InvalidZoneIdException;
import me.eeshe.tempus.mapper.ClientMapper;
import me.eeshe.tempus.mapper.ProjectMapper;
import me.eeshe.tempus.mapper.ReportMapper;
import me.eeshe.tempus.model.ClientReportEntry;
import me.eeshe.tempus.model.ProjectReportEntry;
import me.eeshe.tempus.model.Report;
import me.eeshe.tempus.model.ReportDay;
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
        return new ReportDTO<>(
                report.totalTrackedTimeMillis(),
                report.totalBillableTrackedTimeMillis(),
                report.totalNonBillableTrackedTimeMillis(),
                report.totalAccumulatedPay(),
                report.reportDays().stream()
                        .map(reportDay -> toProjectReportDayDTO(reportDay))
                        .toList());
    }

    @Override
    public ReportDTO<ClientReportEntryDTO> toClientDTO(Report<ClientReportEntry> report) {
        return new ReportDTO<>(
                report.totalTrackedTimeMillis(),
                report.totalBillableTrackedTimeMillis(),
                report.totalNonBillableTrackedTimeMillis(),
                report.totalAccumulatedPay(),
                report.reportDays().stream()
                        .map(reportDay -> toClientReportDayDTO(reportDay))
                        .toList());
    }

    private ReportDayDTO<ProjectReportEntryDTO> toProjectReportDayDTO(ReportDay<ProjectReportEntry> reportDay) {
        final List<ProjectReportEntryDTO> projectReportEntryDTOs = reportDay.reportEntries().stream()
                .map(entry -> new ProjectReportEntryDTO(projectMapper.toDTO(entry.project()),
                        entry.trackedTimeMillis()))
                .toList();
        return new ReportDayDTO<>(
                reportDay.date(),
                reportDay.totalTrackedTimeMillis(),
                reportDay.totalBillableTrackedTimeMillis(),
                reportDay.totalNonBillableTrackedTimeMillis(),
                reportDay.totalAccumulatedPay(),
                projectReportEntryDTOs);
    }

    private ReportDayDTO<ClientReportEntryDTO> toClientReportDayDTO(ReportDay<ClientReportEntry> reportDay) {
        final List<ClientReportEntryDTO> clientReportEntryDTOs = reportDay.reportEntries().stream()
                .map(entry -> new ClientReportEntryDTO(clientMapper.toDTO(entry.client()),
                        entry.trackedTimeMillis()))
                .toList();
        return new ReportDayDTO<>(
                reportDay.date(),
                reportDay.totalTrackedTimeMillis(),
                reportDay.totalBillableTrackedTimeMillis(),
                reportDay.totalNonBillableTrackedTimeMillis(),
                reportDay.totalAccumulatedPay(),
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
                reportRequestDTO.isBillable(),
                parseZoneId(reportRequestDTO.zoneId()));
    }

    private static ZoneId parseZoneId(String zoneId) {
        if (zoneId == null || zoneId.isBlank()) {
            return ZoneOffset.UTC;
        }
        try {
            return ZoneId.of(zoneId);
        } catch (DateTimeException exception) {
            throw new InvalidZoneIdException(zoneId);
        }
    }
}
