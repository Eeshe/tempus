package me.eeshe.tempus.service;

import me.eeshe.tempus.model.Report;
import me.eeshe.tempus.request.ReportRequest;

public interface ReportService {

    Report generateReport(ReportRequest request);
}
