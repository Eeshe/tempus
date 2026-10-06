import { Component, computed, inject, signal } from '@angular/core';
import { ClientReport } from '../../reports/models/report.model';
import { MsToHHMMSSPipe } from '../../shared/pipes/ms-to-hhmmss.pipe';
import { SortableColumn, SortableTable } from '../../shared/sortable-table/sortable-table';
import { ClientModal } from '../client-modal/client-modal';
import { Client } from '../models/client.model';
import { ClientReportStore } from '../store/client-report.store';

@Component({
  imports: [SortableTable, MsToHHMMSSPipe, ClientModal],
  selector: 'app-client-list',
  styleUrl: './client-list.css',
  templateUrl: './client-list.html',
})
export class ClientList {
  private readonly clientReportStore: ClientReportStore = inject(ClientReportStore);

  readonly clientReports = this.clientReportStore.clientReports;

  readonly columns: SortableColumn<ClientReport>[] = [
    {
      key: "client",
      label: "Client",
      align: "left",
      sortValue: (clientReport) => clientReport.client.name,
    },
    {
      key: "duration",
      label: "Duration",
      align: "left",
      sortValue: (clientReport) => clientReport.trackedTimeMillis,
    },
  ];

  readonly openModalClientId = signal<number | null>(null);

  readonly openModalClient = computed<Client | null>(() => {
    const clientId = this.openModalClientId();
    if (clientId === null) {
      return null;
    }
    return this.clientReports().find(clientReport => clientReport.client.id === clientId)?.client ?? null;
  });

  trackBy(_index: number, clientReport: ClientReport): unknown {
    return clientReport.client.id;
  }

  constructor() {
    this.clientReportStore.load();
  }

  openClientModal(client: Client): void {
    this.openModalClientId.set(client.id);
  }

  closeClientModal(): void {
    this.openModalClientId.set(null);
  }
}
