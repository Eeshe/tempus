import { Component, HostListener, input, OnDestroy, output } from '@angular/core';

const openModals: AppModal[] = [];

@Component({
  selector: 'app-modal',
  styleUrl: './modal.css',
  templateUrl: './modal.html',
})
export class AppModal implements OnDestroy {
  readonly header = input<string>();
  readonly ariaLabel = input<string>();
  readonly size = input<'sm' | 'md' | 'lg'>('md');
  readonly closeOnBackdropClick = input<boolean>(true);
  readonly closeOnEscape = input<boolean>(true);

  readonly closeEvent = output<void>();

  constructor() {
    openModals.push(this);
  }

  ngOnDestroy(): void {
    const index: number = openModals.indexOf(this);
    if (index === -1) {
      return;
    }
    openModals.splice(index, 1);
  }

  @HostListener('document:keydown.escape', ['$event'])
  attemptClose(event: Event): void {
    const isTopModal = openModals[openModals.length - 1] === this;
    if (!this.closeOnEscape() || !isTopModal) {
      return;
    }
    if (event.target instanceof HTMLInputElement) {
      (event.target as HTMLElement).blur();
      return;
    }
    this.closeEvent.emit();
  }

  close(): void {
    this.closeEvent.emit();
  }

  onBackdropClick(event: MouseEvent): void {
    if (event.target !== event.currentTarget) {
      return;
    }
    if (!this.closeOnBackdropClick()) {
      return;
    }
    this.close();
  }
}
