import { Component, inject, input, output, signal } from '@angular/core';
import { DeleteConfirmationModal } from '../../shared/delete-confirmation-modal/delete-confirmation-modal';
import { InlineEditInput } from '../../shared/input/inline-edit-input/inline-edit-input';
import { AppModal } from '../../shared/modal/modal';
import { ModalBase } from '../../shared/modal/modal-base';
import { ClientProjectList } from '../client-project-list/client-project-list';
import { Client } from '../models/client.model';
import { ClientReportStore } from '../store/client-report.store';

@Component({
  imports: [AppModal, InlineEditInput, DeleteConfirmationModal, ClientProjectList],
  selector: 'app-client-modal',
  styleUrl: './client-modal.css',
  templateUrl: './client-modal.html',
})
export class ClientModal extends ModalBase {
  private readonly clientReportStore: ClientReportStore = inject(ClientReportStore);

  readonly client = input.required<Client>();

  readonly clientDeleteEvent = output<Client>();

  readonly isEditingName = signal<boolean>(false);
  readonly isDeleteConfirmationModalOpen = signal<boolean>(false);

  startNameEdit(): void {
    this.isEditingName.set(true);
  }

  editName(newName: string): void {
    this.clientReportStore.editClientName(this.client(), newName);
    this.stopNameEdit();
  }

  stopNameEdit(): void {
    this.isEditingName.set(false);
  }

  toggleDeleteConfirmationModal(): void {
    this.isDeleteConfirmationModalOpen.update(value => !value);
  }

  handleDeleteConfirmation(confirmedDeletion: boolean): void {
    if (confirmedDeletion) {
      this.clientReportStore.delete(this.client());
      this.clientDeleteEvent.emit(this.client());
    }
    this.toggleDeleteConfirmationModal();
  }
}
