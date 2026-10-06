import { Component, signal } from '@angular/core';
import { NewTimeEntryFormModal } from '../new-time-entry-form-modal/new-time-entry-form-modal';

@Component({
  imports: [NewTimeEntryFormModal],
  selector: 'app-new-time-entry-button',
  styleUrl: './new-time-entry-button.css',
  templateUrl: './new-time-entry-button.html',
})
export class NewTimeEntryButton {
  readonly isNewTimeEntryFormModalOpen = signal<boolean>(false);

  toggleNewTimeEntryFormModal(): void {
    this.isNewTimeEntryFormModalOpen.update(value => !value);
  }
}
