import { HttpClient } from "@angular/common/http";
import { inject, Service } from "@angular/core";
import { Observable } from "rxjs";
import { Project } from "../../project/models/project.model";
import { Task } from "../models/task.model";

interface CreateTaskRequest {
  name: string;
  projectId: number;
}

interface PatchTaskRequest {
  name: string | null,
}

@Service()
export class TaskService {
  private readonly url: string = "/api/v1/tasks";
  private http: HttpClient = inject(HttpClient);

  createTask(name: string, project: Project): Observable<Task> {
    const projectId: number = project.id;
    const createTaskRequest: CreateTaskRequest = {
      name: name,
      projectId: projectId,
    };
    return this.http.post<Task>(this.url, createTaskRequest, { withCredentials: true });
  }

  patchTaskName(task: Task, newName: string): Observable<Task> {
    const patchTaskRequest: Partial<PatchTaskRequest> = {
      name: newName,
    }
    return this.http.patch<Task>(`${this.url}/${task.id}`, patchTaskRequest, { withCredentials: true });
  }

  deleteTask(task: Task): Observable<void> {
    return this.http.delete<void>(`${this.url}/${task.id}`, { withCredentials: true });
  }
}
