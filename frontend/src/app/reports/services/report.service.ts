import { HttpClient } from "@angular/common/http";
import { inject, Service } from "@angular/core";
import { Observable } from "rxjs";
import { toInstantEnd, toInstantStart } from "../../shared/util/date.util";
import { Client } from "../../client/models/client.model";
import { Project } from "../../project/models/project.model";
import { Task } from "../../task/models/task.model";
import { ClientReport, ProjectReport, Report } from "../models/report.model";

interface ReportRequest {
  startDate: string | null,
  endDate: string | null,
  projectIds: number[] | null,
  taskIds: number[] | null,
  clientIds: number[] | null,
  descriptions: string[] | null,
  isBillable: boolean | null,
}

@Service()
export class ReportService {
  private readonly url: string = "/api/v1/reports";
  private readonly http: HttpClient = inject(HttpClient);

  generateProjectReport(
    startDate: string | null,
    endDate: string | null,
    projects: Project[],
    tasks: Task[],
    clients: Client[],
    descriptions: string[],
    isBillable: boolean | null
  ): Observable<Report<ProjectReport>> {
    return this.generateReport<ProjectReport>(
      "projects",
      startDate,
      endDate,
      projects,
      tasks,
      clients,
      descriptions,
      isBillable
    );
  }

  generateAllTimeProjectReportByProject(project: Project): Observable<Report<ProjectReport>> {
    return this.generateProjectReport(null, null, [project], [], [], [], null);
  }

  generateAllTimeProjectReportByClient(client: Client): Observable<Report<ProjectReport>> {
    return this.generateProjectReport(null, null, [], [], [client], [], null);
  }

  generateClientReport(
    startDate: string | null,
    endDate: string | null,
    projects: Project[],
    tasks: Task[],
    clients: Client[],
    descriptions: string[],
    isBillable: boolean | null
  ): Observable<Report<ClientReport>> {
    return this.generateReport<ClientReport>(
      "clients",
      startDate,
      endDate,
      projects,
      tasks,
      clients,
      descriptions,
      isBillable
    );
  }

  generateAllTimeClientReportByClient(client: Client): Observable<Report<ClientReport>> {
    return this.generateClientReport(null, null, [], [], [client], [], null);
  }

  private generateReport<T>(
    endpoint: string,
    startDate: string | null,
    endDate: string | null,
    projects: Project[],
    tasks: Task[],
    clients: Client[],
    descriptions: string[],
    isBillable: boolean | null
  ): Observable<Report<T>> {
    const projectIds: number[] = projects.map(project => project.id);
    const taskIds: number[] = tasks.map(task => task.id);
    const clientIds: number[] = clients.map(client => client.id);
    const reportRequest: ReportRequest = {
      startDate: toInstantStart(startDate),
      endDate: toInstantEnd(endDate),
      projectIds,
      taskIds,
      clientIds,
      descriptions,
      isBillable
    }
    return this.http.post<Report<T>>(`${this.url}/${endpoint}`, reportRequest, { withCredentials: true });
  }
}
