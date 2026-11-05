import { formatNumber } from '@angular/common';
import { Component, computed, inject, input, LOCALE_ID } from '@angular/core';
import { MsToHHMMSSPipe } from '../../../shared/pipes/ms-to-hhmmss.pipe';
import { SortableColumn, SortableTable } from '../../../shared/sortable-table/sortable-table';
import { durationFromMs, formatHHMMSSTime } from '../../../shared/util/time.util';
import { flattenProjectReportEntries, ProjectReport, Report } from '../../models/report.model';

@Component({
  imports: [SortableTable, MsToHHMMSSPipe],
  selector: 'app-report-details-list',
  styleUrl: './report-details-list.css',
  templateUrl: './report-details-list.html',
})
export class ReportDetailsList {
  private readonly localeId: string = inject(LOCALE_ID);

  readonly report = input.required<Report<ProjectReport>>();

  readonly reportEntries = computed<ProjectReport[]>(() => flattenProjectReportEntries(this.report()));

  readonly columns: SortableColumn<ProjectReport>[] = [
    {
      key: "project",
      label: $localize`:Table column|Project name:Project`,
      align: "left",
      sortValue: (entry) => entry.project.name,
    },
    {
      key: "duration",
      label: $localize`:Table column|Tracked duration:Duration`,
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
    return formatNumber(this.report().totalAccumulatedPay, this.localeId, '1.2-2');
  });
}
