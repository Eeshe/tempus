package me.eeshe.tempus.mapper.impl;

import org.springframework.stereotype.Component;

import me.eeshe.tempus.dto.ReportDTO;
import me.eeshe.tempus.dto.ReportRequestDTO;
import me.eeshe.tempus.mapper.ReportMapper;
import me.eeshe.tempus.mapper.TimeEntryMapper;
import me.eeshe.tempus.model.Report;
import me.eeshe.tempus.request.ReportRequest;

@Component
public class ReportMapperImpl implements ReportMapper {
    private final TimeEntryMapper timeEntryMapper;

    public ReportMapperImpl(TimeEntryMapper timeEntryMapper) {
        this.timeEntryMapper = timeEntryMapper;
    }

    @Override
    public ReportDTO toDTO(Report report) {
        return new ReportDTO(
                report.getTimeEntries().stream().map(timeEntryMapper::toDTO).toList(),
                report.getTotalTrackedTimeMillis());
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
