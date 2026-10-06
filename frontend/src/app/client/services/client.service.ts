import { HttpClient } from "@angular/common/http";
import { inject, Service } from "@angular/core";
import { Observable } from "rxjs";
import { Client } from "../models/client.model";

interface CreateClientRequest {
  name: string,
}

interface PatchClientRequest {
  name: string,
}

@Service()
export class ClientService {
  private readonly url: string = "/api/v1/clients";
  private http: HttpClient = inject(HttpClient);

  listClients(): Observable<Client[]> {
    return this.http.get<Client[]>(this.url, { withCredentials: true });
  }

  createClient(name: string): Observable<Client> {
    const createClientRequest: CreateClientRequest = {
      name
    };
    return this.http.post<Client>(this.url, createClientRequest, { withCredentials: true });
  }

  patchClientName(client: Client, newName: string): Observable<Client> {
    const patchRequest: Partial<PatchClientRequest> = {
      name: newName
    }
    return this.http.patch<Client>(`${this.url}/${client.id}`, patchRequest, { withCredentials: true });
  }

  deleteClient(client: Client): Observable<void> {
    return this.http.delete<void>(`${this.url}/${client.id}`, { withCredentials: true });
  }
}
