import { Component, computed, input } from '@angular/core';
import { MsToHHMMSSPipe } from '../../../shared/pipes/ms-to-hhmmss.pipe';
import { SortableColumn, SortableTable } from '../../../shared/sortable-table/sortable-table';
import { durationFromMs, formatHHMMSSTime } from '../../../shared/util/time.util';
import { ProjectReport, Report } from '../../models/report.model';

@Component({
  imports: [SortableTable, MsToHHMMSSPipe],
  selector: 'app-report-details-list',
  styleUrl: './report-details-list.css',
  templateUrl: './report-details-list.html',
})
export class ReportDetailsList {
  readonly report = input.required<Report<ProjectReport>>();

  readonly columns: SortableColumn<ProjectReport>[] = [
    {
      key: "project",
      label: "Project",
      align: "left",
      sortValue: (entry) => entry.project.name,
    },
    {
      key: "duration",
      label: "Duration",
      align: "right",
      sortValue: (entry) => entry.trackedTimeMillis,
    },
  ];

  trackBy(_index: number, projectReport: ProjectReport): unknown {
    return projectReport.project.id;
  }

  readonly formattedTotalTrackedTime = computed<string>(() => {
    return formatHHMMSSTime(durationFromMs(this.report().totalTrackedTimeMillis));
  });

  readonly formattedTotalBillableTrackedTime = computed<string>(() => {
    return formatHHMMSSTime(durationFromMs(this.report().totalBillableTrackedTimeMillis));
  });

  readonly formattedTotalNonBillableTrackedTime = computed<string>(() => {
    return formatHHMMSSTime(durationFromMs(this.report().totalNonBillableTrackedTimeMillis));
  });

  readonly formattedTotalAccumulatedPay = computed<string>(() => {
    return this.report().totalAccumulatedPay.toFixed(2);
  });
}
