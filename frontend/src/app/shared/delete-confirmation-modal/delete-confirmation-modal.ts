import { Component, computed, input, output } from '@angular/core';
import { AppModal } from '../modal/modal';
import { ModalBase } from '../modal/modal-base';

/**
 * Confirmation dialog for destructive actions. Defaults to the "delete <name>" wording, but the
 * header and confirm button label can be overridden for other destructive flows (for example
 * discarding unsaved settings changes).
 */
@Component({
  imports: [AppModal],
  selector: 'app-delete-confirmation-modal',
  styleUrl: './delete-confirmation-modal.css',
  templateUrl: './delete-confirmation-modal.html',
})
export class DeleteConfirmationModal extends ModalBase {

  readonly toDeleteName = input<string>('');
  readonly headerOverride = input<string | null>(null);
  readonly confirmLabel = input<string>($localize`:Delete confirmation|Confirm button:Delete`);

  readonly header = computed<string>(() =>
    this.headerOverride() ??
    $localize`:Delete confirmation|Question shown before deleting an item:Are you sure you want to delete ${this.toDeleteName()}:name:?`,
  );

  readonly confirmationOptionSelectEvent = output<boolean>();
}
