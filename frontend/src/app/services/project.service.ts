import { HttpClient } from "@angular/common/http";
import { inject, Service } from "@angular/core";
import { Observable } from "rxjs";
import { Client } from "../client/models/client.model";
import { Project } from "../project/models/project.model";

interface CreateProjectRequest {
  name: string;
  clientId: number | null;
  hourlyRate: number | null;
}

interface PatchProjectRequest {
  name: string | null;
  isArchived: boolean | null;
  clientId: number | null;
  hourlyRate: number | null;
}

@Service()
export class ProjectService {
  private readonly url: string = "/api/v1/projects";
  private http: HttpClient = inject(HttpClient);

  listProjects(): Observable<Project[]> {
    return this.http.get<Project[]>(this.url, { withCredentials: true });
  }

  createProject(name: string, client: Client | null, hourlyRate: number | null): Observable<Project> {
    const clientId: number | null = client?.id ?? null;
    const createProjectRequest: CreateProjectRequest = {
      name: name,
      clientId: clientId,
      hourlyRate: hourlyRate,
    };
    return this.http.post<Project>(this.url, createProjectRequest, { withCredentials: true });
  }

  patchProjectName(project: Project, newName: string): Observable<Project> {
    const patchProjectRequest: Partial<PatchProjectRequest> = {
      name: newName,
    }
    return this.http.patch<Project>(`${this.url}/${project.id}`, patchProjectRequest, { withCredentials: true });
  }

  patchProjectClient(project: Project, newClient: Client | null): Observable<Project> {
    const clientId: number | null = newClient?.id ?? null;
    const patchProjectRequest: Partial<PatchProjectRequest> = {
      clientId
    }
    return this.http.patch<Project>(`${this.url}/${project.id}`, patchProjectRequest, { withCredentials: true });
  }

  patchProjectHourlyRate(project: Project, newHourlyRate: number | null): Observable<Project> {
    const patchProjectRequest: Partial<PatchProjectRequest> = {
      hourlyRate: newHourlyRate,
    }
    return this.http.patch<Project>(`${this.url}/${project.id}`, patchProjectRequest, { withCredentials: true });
  }

  patchProjectArchived(project: Project, newIsArchived: boolean): Observable<Project> {
    const patchProjectRequest: Partial<PatchProjectRequest> = {
      isArchived: newIsArchived,
    }
    return this.http.patch<Project>(`${this.url}/${project.id}`, patchProjectRequest, { withCredentials: true });
  }

  deleteProject(project: Project): Observable<void> {
    return this.http.delete<void>(`${this.url}/${project.id}`, { withCredentials: true });
  }
}
