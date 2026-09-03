package me.eeshe.tempus.mapper;

import me.eeshe.tempus.dto.ReportDTO;
import me.eeshe.tempus.dto.ReportRequestDTO;
import me.eeshe.tempus.model.Report;
import me.eeshe.tempus.request.ReportRequest;

public interface ReportMapper {

    ReportDTO toDTO(Report report);

    ReportRequest fromDTO(ReportRequestDTO reportRequestDTO);
}
