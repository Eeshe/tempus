import { Component, signal } from '@angular/core';
import { ImportModal } from './import-modal/import-modal';

@Component({
  imports: [ImportModal],
  selector: 'app-migration-settings',
  styleUrl: './migration-settings.css',
  templateUrl: './migration-settings.html',
})
export class MigrationSettings {
  readonly isImportModalOpen = signal<boolean>(false);

  toggleImportModal(): void {
    this.isImportModalOpen.update((value) => !value);
  }
}
