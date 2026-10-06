import { HttpClient } from "@angular/common/http";
import { inject, Service } from "@angular/core";
import { Observable } from "rxjs";
import { SyncData } from "../model/sync-data.model";

@Service()
export class SyncDataService {
  private readonly url: string = "/api/v1/sync";
  private http: HttpClient = inject(HttpClient);

  getSyncData(): Observable<SyncData> {
    return this.http.get<SyncData>(this.url, { withCredentials: true });
  }

  triggerExport(): Observable<void> {
    return this.http.post<void>(`${this.url}/export`, { withCredentials: true });
  }

  triggerImport(): Observable<void> {
    return this.http.post<void>(`${this.url}/import`, { withCredentials: true });
  }
}
