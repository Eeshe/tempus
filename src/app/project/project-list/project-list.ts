import { Component, computed, inject, signal } from '@angular/core';
import { ProjectReport } from '../../reports/models/report.model';
import { MsToHHMMSSPipe } from '../../shared/pipes/ms-to-hhmmss.pipe';
import { SortableColumn, SortableTable } from '../../shared/sortable-table/sortable-table';
import { Project } from '../models/project.model';
import { ProjectModal } from '../project-modal/project-modal';
import { ProjectReportStore } from '../store/project-report.store';

@Component({
  imports: [SortableTable, MsToHHMMSSPipe, ProjectModal],
  selector: 'app-project-list',
  styleUrl: './project-list.css',
  templateUrl: './project-list.html',
})
export class ProjectList {
  private readonly projectReportStore: ProjectReportStore = inject(ProjectReportStore);

  readonly projectReports = this.projectReportStore.projectReports;

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
    {
      key: "client",
      label: "Client",
      align: "left",
      sortValue: (projectReport) => projectReport.project.client?.name ?? "",
    },
    {
      key: "hourlyRate",
      label: "Hourly Rate (USD)",
      align: "center",
      cellAlign: "center",
      sortValue: (projectReport) => projectReport.project.hourlyRate ?? -1,
    },
    {
      key: "accumulatedPay",
      label: "Accumulated Pay (USD)",
      align: "center",
      cellAlign: "center",
      sortValue: (projectReport) => this.calculateAccumulatedPay(projectReport),
    },
  ];

  readonly openModalProjectId = signal<number | null>(null);

  computeProjectSortGroup(projectReport: ProjectReport): number {
    return projectReport.project.isArchived ? 1 : 0;
  }

  computeProjectRowClass(projectReport: ProjectReport): string | null {
    return projectReport.project.isArchived ? 'sortable-table__row--archived' : null;
  }

  readonly openModalProject = computed<Project | null>(() => {
    const projectId = this.openModalProjectId();
    if (projectId === null) {
      return null;
    }
    return this.projectReports().find(projectReport => projectReport.project.id === projectId)?.project ?? null;
  });

  trackBy(_index: number, projectReport: ProjectReport): unknown {
    return projectReport.project.id;
  }

  constructor() {
    this.projectReportStore.load();
  }

  openProjectModal(project: Project): void {
    this.openModalProjectId.set(project.id);
  }

  closeProjectModal(): void {
    this.openModalProjectId.set(null);
  }

  calculateAccumulatedPay(projectReport: ProjectReport): number {
    const hourlyRate: number | null = projectReport.project.hourlyRate;
    if (hourlyRate == null) {
      return -1;
    }
    const trackedHours: number = projectReport.trackedTimeMillis / 1000 / 60 / 60;

    return parseFloat((hourlyRate * trackedHours).toFixed(2));
  }
}
