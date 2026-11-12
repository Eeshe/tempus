import { Component, computed, inject, input, output, signal } from '@angular/core';
import { Client } from '../../../../client/models/client.model';
import { ClientService } from '../../../../client/services/client.service';
import { SearchInput } from '../../../../shared/input/search-input/search-input';
import { PopupSelectorBase } from '../../../../shared/selector/popup-selector-base';

@Component({
  imports: [SearchInput],
  selector: 'app-client-filter',
  styleUrl: './client-filter.css',
  templateUrl: './client-filter.html',
})
export class ClientFilter extends PopupSelectorBase {
  private readonly clientService = inject(ClientService);

  readonly selectedClients = input<Client[]>([]);

  readonly selectedClientsChangeEvent = output<Client[]>();

  readonly clients = signal<Client[]>([]);

  readonly filteredClients = computed<Client[]>(() =>
    this.clients().filter((client) => this.matchesSearch(client.name))
  );

  readonly allSelected = computed<boolean>(() => {
    const all: Client[] = this.filteredClients();
    const selected: Client[] = this.selectedClients();

    return all.length > 0 && all.length === selected.length;
  });

  protected override openPopup(): void {
    super.openPopup();

    this.clientService.listClients().subscribe((fetchedClients) =>
      this.clients.set(
        [...fetchedClients].sort((a, b) =>
          a.name.localeCompare(b.name, undefined, { sensitivity: 'base' })
        )
      )
    );
  }

  isSelected(checkClient: Client): boolean {
    return this.selectedClients().some((client) => client.id === checkClient.id);
  }

  toggleClient(clientToggle: Client): void {
    const current: Client[] = this.selectedClients();
    const updated: Client[] = this.isSelected(clientToggle)
      ? current.filter((client) => client.id !== clientToggle.id)
      : [...current, clientToggle];

    this.selectedClientsChangeEvent.emit(updated);
  }

  toggleSelectAll(): void {
    this.selectedClientsChangeEvent.emit(this.allSelected() ? [] : [...this.filteredClients()]);
  }
}
