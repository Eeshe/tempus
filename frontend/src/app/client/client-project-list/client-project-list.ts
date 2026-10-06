import { Component, effect, inject, input, signal } from '@angular/core';
import { ProjectReport } from '../../reports/models/report.model';
import { ReportService } from '../../reports/services/report.service';
import { MsToHHMMSSPipe } from '../../shared/pipes/ms-to-hhmmss.pipe';
import { SortableColumn, SortableTable } from '../../shared/sortable-table/sortable-table';
import { Client } from '../models/client.model';

@Component({
  imports: [SortableTable, MsToHHMMSSPipe],
  selector: 'app-client-project-list',
  styleUrl: './client-project-list.css',
  templateUrl: './client-project-list.html',
})
export class ClientProjectList {
  private readonly reportService: ReportService = inject(ReportService);

  readonly client = input.required<Client>();
  readonly projectReports = signal<ProjectReport[]>([]);

  readonly columns: SortableColumn<ProjectReport>[] = [
    {
      key: "project",
      label: "Project",
      align: "left",
      sortValue: (projectReport) => projectReport.project.name,
    },
    {
      key: "duration",
      label: "Duration",
      align: "left",
      sortValue: (projectReport) => projectReport.trackedTimeMillis,
    },
  ];

  constructor() {
    effect(() => {
      const client: Client = this.client();
      this.reportService.generateAllTimeProjectReportByClient(client).subscribe(report =>
        this.projectReports.set(report.reportEntries));
    });
  }

  trackBy(_index: number, projectReport: ProjectReport): unknown {
    return projectReport.project.id;
  }
}
