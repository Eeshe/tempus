package me.eeshe.tempus.mapper;

import me.eeshe.tempus.dto.ClientReportEntryDTO;
import me.eeshe.tempus.dto.ProjectReportEntryDTO;
import me.eeshe.tempus.dto.ReportDTO;
import me.eeshe.tempus.dto.ReportRequestDTO;
import me.eeshe.tempus.model.ClientReportEntry;
import me.eeshe.tempus.model.ProjectReportEntry;
import me.eeshe.tempus.model.Report;
import me.eeshe.tempus.request.ReportRequest;

public interface ReportMapper {

    ReportDTO<ProjectReportEntryDTO> toProjectDTO(Report<ProjectReportEntry> report);

    ReportDTO<ClientReportEntryDTO> toClientDTO(Report<ClientReportEntry> report);

    ReportRequest fromDTO(ReportRequestDTO reportRequestDTO, long userId);
}
