import { Directive, output } from '@angular/core';

@Directive({
  standalone: true,
  host: {
    'animate.enter': 'modal-transition modal-transition--enter',
    'animate.leave': 'modal-transition modal-transition--leave',
  },
})
export abstract class ModalBase {
  readonly closeEvent = output<void>();

  close(): void {
    this.closeEvent.emit();
  }
}