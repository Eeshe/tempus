import { Component, input, output } from '@angular/core';
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

  readonly confirmationOptionSelectEvent = output<boolean>();
}
