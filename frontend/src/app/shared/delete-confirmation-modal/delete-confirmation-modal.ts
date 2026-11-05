import { Component, computed, input, output } from '@angular/core';
import { AppModal } from '../modal/modal';
import { ModalBase } from '../modal/modal-base';

@Component({
  imports: [AppModal],
  selector: 'app-delete-confirmation-modal',
  styleUrl: './delete-confirmation-modal.css',
  templateUrl: './delete-confirmation-modal.html',
})
export class DeleteConfirmationModal extends ModalBase {

  readonly toDeleteName = input.required<string>();

  readonly header = computed<string>(() =>
    $localize`:Delete confirmation|Question shown before deleting an item:Are you sure you want to delete ${this.toDeleteName()}:name:?`,
  );

  readonly confirmationOptionSelectEvent = output<boolean>();
}
