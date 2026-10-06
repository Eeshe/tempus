import { HttpClient, HttpParams } from "@angular/common/http";
import { inject, Service } from "@angular/core";
import { Observable } from "rxjs";
import { Project } from "../../project/models/project.model";
import { Task } from "../../task/models/task.model";
import { TimeEntryPage } from "../models/time-entry-page.model";
import { TimeEntry } from "../models/time-entry.model";

interface CreateTimeEntryRequest {
  projectId: number;
  taskId: number | null;
  description: string | null;
  isBillable: boolean;
  startTime: string;
  endTime: string | null;
}

interface PatchTimeEntryRequest {
  userId: number
  projectId: number
  taskId: number | null,
  description: string
  isBillable: boolean
  startTime: string
  endTime: string
}

@Service()
export class TimeEntryService {
  private readonly url: string = "/api/v1/time-entries";
  private http: HttpClient = inject(HttpClient);

  listTimeEntries(cursor: string | null): Observable<TimeEntryPage> {
    let params: HttpParams = new HttpParams().set("size", 50);

    if (cursor != null) {
      params = params.set("cursor", cursor);
    }
    return this.http.get<TimeEntryPage>(this.url, { params: params, withCredentials: true });
  }

  createTimeEntry(
    projectId: number,
    taskId: number | null,
    description: string | null,
    isBillable: boolean,
    startTime: string,
    endTime: string | null
  ): Observable<TimeEntry> {
    const createRequest: CreateTimeEntryRequest = {
      projectId,
      taskId,
      description,
      isBillable,
      startTime,
      endTime
    };
    return this.http.post<TimeEntry>(this.url, createRequest, { withCredentials: true });
  }

  resumeTimeEntry(timeEntry: TimeEntry): Observable<TimeEntry> {
    const createRequest: CreateTimeEntryRequest = {
      projectId: timeEntry.project.id,
      taskId: timeEntry.task ? timeEntry.task.id : null,
      description: timeEntry.description,
      isBillable: timeEntry.isBillable,
      startTime: new Date().toISOString(),
      endTime: null,
    };
    return this.http.post<TimeEntry>(this.url, createRequest, { withCredentials: true });
  }

  patchTimeEntryDescription(timeEntry: TimeEntry, newDescription: string): Observable<TimeEntry> {
    const patchRequest: Partial<PatchTimeEntryRequest> = {
      description: newDescription,
    }
    return this.patchTimeEntry(timeEntry, patchRequest);
  }

  patchTimeEntryBillable(timeEntry: TimeEntry, newBillableStatus: boolean): Observable<TimeEntry> {
    const patchRequest: Partial<PatchTimeEntryRequest> = {
      isBillable: newBillableStatus,
    }
    return this.patchTimeEntry(timeEntry, patchRequest);
  }

  patchTimeEntryStartTime(timeEntry: TimeEntry, startTime: Date): Observable<TimeEntry> {
    const patchRequest: Partial<PatchTimeEntryRequest> = {
      startTime: startTime.toISOString(),
    }
    return this.patchTimeEntry(timeEntry, patchRequest)
  }

  patchTimeEntryEndTime(timeEntry: TimeEntry, endTime: Date): Observable<TimeEntry> {
    const patchRequest: Partial<PatchTimeEntryRequest> = {
      endTime: endTime.toISOString(),
    }
    return this.patchTimeEntry(timeEntry, patchRequest)
  }

  patchTimeEntryProject(timeEntry: TimeEntry, project: Project): Observable<TimeEntry> {
    const patchRequest: Partial<PatchTimeEntryRequest> = {
      projectId: project.id,
      taskId: null,
    }
    return this.patchTimeEntry(timeEntry, patchRequest)
  }

  patchTimeEntryTask(timeEntry: TimeEntry, project: Project, task: Task): Observable<TimeEntry> {
    const patchRequest: Partial<PatchTimeEntryRequest> = {
      projectId: project.id,
      taskId: task.id,
    }
    return this.patchTimeEntry(timeEntry, patchRequest)
  }

  private patchTimeEntry(timeEntry: TimeEntry, patchRequest: Partial<PatchTimeEntryRequest>): Observable<TimeEntry> {
    return this.http.patch<TimeEntry>(`${this.url}/${timeEntry.id}`, patchRequest, { withCredentials: true })
  }

  deleteTimeEntry(timeEntry: TimeEntry): Observable<void> {
    return this.http.delete<void>(`${this.url}/${timeEntry.id}`, { withCredentials: true });
  }
}
