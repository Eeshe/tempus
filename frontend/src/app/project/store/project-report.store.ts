import { inject, Service, Signal, signal } from "@angular/core";
import { Client } from "../../client/models/client.model";
import { ProjectReport } from "../../reports/models/report.model";
import { ReportService } from "../../reports/services/report.service";
import { ProjectService } from "../../services/project.service";
import { Task } from "../../task/models/task.model";
import { TaskService } from "../../task/services/task.service";
import { Project } from "../models/project.model";

@Service()
export class ProjectReportStore {
  private readonly projectService: ProjectService = inject(ProjectService);
  private readonly reportService: ReportService = inject(ReportService);
  private readonly taskService: TaskService = inject(TaskService);

  private readonly _projectReports = signal<ProjectReport[]>([]);

  readonly projectReports: Signal<ProjectReport[]> = this._projectReports.asReadonly();

  load(): void {
    this.projectService.listProjects().subscribe(projects => this.reportService.generateProjectReport(
      null,
      null,
      projects,
      [],
      [],
      [],
      null
    ).subscribe(report => {
      let projectReports: ProjectReport[] = report.reportEntries;
      projectReports = this.populateMissingProjects(projects, projectReports);

      this._projectReports.set(projectReports);
    }));
  }

  // Since ProjectReports are based on time entries, projects with no tracked time won't be shown
  // To fix this, we add the missing projects manually with a tracked time of 0
  private populateMissingProjects(projects: Project[], projectReports: ProjectReport[]): ProjectReport[] {
    const projectIds: Set<number> = new Set(projectReports.map(projectReport => projectReport.project.id));
    for (const project of projects) {
      if (projectIds.has(project.id)) {
        continue;
      }
      projectReports.push({
        project: project,
        trackedTimeMillis: 0,
      });
    }
    return projectReports;
  }

  editProjectName(project: Project, newName: string): void {
    this.projectService.patchProjectName(project, newName).subscribe(patchedProject =>
      this.replace(patchedProject));
  }

  editProjectClient(project: Project, newClient: Client | null): void {
    this.projectService.patchProjectClient(project, newClient).subscribe(patchedProject =>
      this.replace(patchedProject));
  }

  editProjectHourlyRate(project: Project, newHourlyRate: number | null): void {
    this.projectService.patchProjectHourlyRate(project, newHourlyRate).subscribe(patchedProject =>
      this.replace(patchedProject));
  }

  editProjectArchived(project: Project, isArchived: boolean): void {
    this.projectService.patchProjectArchived(project, isArchived).subscribe(patchedProject =>
      this.replace(patchedProject));
  }

  editProjectTask(project: Project, task: Task): void {
    this.taskService.patchTaskName(task, task.name).subscribe(patchedTask => {
      const updatedProject: Project = {
        ...project,
        tasks: project.tasks.map(previousTask => previousTask.id !== patchedTask.id ? previousTask : patchedTask),
      };
      this.replace(updatedProject);
    })
  }

  deleteProjectTask(project: Project, task: Task): void {
    this.taskService.deleteTask(task).subscribe(() => {
      const updatedProject: Project = {
        ...project,
        tasks: project.tasks.filter(previousTask => previousTask.id !== task.id),
      };

      this.replace(updatedProject);
    });
  }

  delete(project: Project): void {
    this.projectService.deleteProject(project).subscribe(() =>
      this._projectReports.update(projectReports => projectReports.filter(projectReport => projectReport.project.id !== project.id))
    );
  }

  private replace(updatedProject: Project) {
    this._projectReports.update(projectReports =>
      projectReports.map(projectReport =>
        projectReport.project.id !== updatedProject.id
          ? projectReport
          : { ...projectReport, project: updatedProject }
      )
    );
  }
}
