import { Component, computed, inject, input, output, signal } from '@angular/core';
import { PopupSelectorBase } from '../../shared/selector/popup-selector-base';
import { CreateClientFormModal } from '../create-client-form-modal/create-client-form-modal';
import { Client } from '../models/client.model';
import { ClientService } from '../services/client.service';

@Component({
  imports: [CreateClientFormModal],
  selector: 'app-client-selector-button',
  styleUrl: './client-selector-button.css',
  templateUrl: './client-selector-button.html',
})
export class ClientSelectorButton extends PopupSelectorBase {
  private readonly clientService: ClientService = inject(ClientService);

  readonly selectedClient = input<Client | null>(null);
  readonly displayText = computed<string>(() => {
    return this.selectedClient() == null ? "None" : this.selectedClient()!.name;
  });

  readonly clients = signal<Client[]>([]);
  readonly isCreateClientModalOpen = signal<boolean>(false);

  readonly clientSelectEvent = output<Client | null>();

  protected override openPopup(): void {
    super.openPopup();

    this.clientService.listClients().subscribe(clients =>
      this.clients.set(clients.sort((clientA, clientB) =>
        clientA.name.localeCompare(clientB.name, undefined, { sensitivity: "base" }))));
  }

  changeClient(client: Client | null): void {
    this.clientSelectEvent.emit(client);
    this.toggle();
  }

  toggleCreateClientFormModal(): void {
    this.isCreateClientModalOpen.update(value => !value);
  }
}
