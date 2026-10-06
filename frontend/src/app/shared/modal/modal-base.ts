import { Directive, output } from '@angular/core';

@Directive({
  standalone: true,
})
export abstract class ModalBase {
  readonly closeEvent = output<void>();

  close(): void {
    this.closeEvent.emit();
  }
}