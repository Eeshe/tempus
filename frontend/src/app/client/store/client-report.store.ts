import { inject, Service, Signal, signal } from "@angular/core";
import { ClientReport } from "../../reports/models/report.model";
import { ReportService } from "../../reports/services/report.service";
import { Client } from "../models/client.model";
import { ClientService } from "../services/client.service";

@Service()
export class ClientReportStore {
  private readonly clientService: ClientService = inject(ClientService);
  private readonly reportService: ReportService = inject(ReportService);

  private readonly _clientReports = signal<ClientReport[]>([]);

  readonly clientReports: Signal<ClientReport[]> = this._clientReports.asReadonly();

  load(): void {
    this.clientService.listClients().subscribe(clients =>
      this.reportService.generateClientReport(
        null,
        null,
        [],
        [],
        clients,
        [],
        null
      ).subscribe(report => {
        const clientReports: ClientReport[] = this.populateMissingClients(clients, report.reportEntries);

        this._clientReports.set(clientReports);
      }));
  }

  // Since ClientReports are based on time entries, clients with no tracked time won't be shown
  // To fix this, we add the missing clients manually with a tracked time of 0
  private populateMissingClients(clients: Client[], clientReports: ClientReport[]): ClientReport[] {
    const clientIds: Set<number> = new Set(clientReports.map(clientReport => clientReport.client.id));
    for (const client of clients) {
      if (clientIds.has(client.id)) {
        continue;
      }
      clientReports.push({
        client: client,
        trackedTimeMillis: 0,
      });
    }
    return clientReports;
  }

  editClientName(client: Client, newName: string): void {
    this.clientService.patchClientName(client, newName).subscribe(patchedClient =>
      this.replace(patchedClient));
  }

  delete(client: Client): void {
    this.clientService.deleteClient(client).subscribe(() => this._clientReports.update(clientReports =>
      clientReports.filter(clientReport => clientReport.client.id !== client.id)));
  }

  private replace(updatedClient: Client): void {
    this._clientReports.update(clientReports =>
      clientReports.map(clientReport =>
        clientReport.client.id !== updatedClient.id
          ? clientReport
          : { ...clientReport, client: updatedClient }
      )
    );
  }
}
