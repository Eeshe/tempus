import { Component, signal } from '@angular/core';
import { ProjectReport, Report } from '../models/report.model';
import { ReportDetailsList } from './report-details-list/report-details-list';
import { ReportFiltersBar } from './report-filters-bar/report-filters-bar';
import { ReportGraph } from './report-graph/report-graph';

@Component({
  imports: [ReportFiltersBar, ReportDetailsList, ReportGraph],
  selector: 'app-reports-page',
  styleUrl: './reports-page.css',
  templateUrl: './reports-page.html',
})
export class ReportsPage {
  readonly currentReport = signal<Report<ProjectReport>>({
    startDate: null,
    endDate: null,
    totalTrackedTimeMillis: 0,
    totalBillableTrackedTimeMillis: 0,
    totalNonBillableTrackedTimeMillis: 0,
    totalAccumulatedPay: 0,
    reportDays: []
  });
}
