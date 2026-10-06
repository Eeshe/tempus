package me.eeshe.tempus.service;

import me.eeshe.tempus.model.ClientReportEntry;
import me.eeshe.tempus.model.ProjectReportEntry;
import me.eeshe.tempus.model.Report;
import me.eeshe.tempus.request.ReportRequest;

public interface ReportService {

    Report<ProjectReportEntry> generateProjectReport(ReportRequest reportRequest);

    Report<ClientReportEntry> generateClientReport(ReportRequest reportRequest);
}
