import { Component, ElementRef, HostListener, inject, input, OnDestroy, output, signal } from '@angular/core';

const openModals: AppModal[] = [];

@Component({
  selector: 'app-modal',
  styleUrl: './modal.css',
  templateUrl: './modal.html',
})
export class AppModal implements OnDestroy {
  /** The `<app-modal>` host element, used to tell a modal stacked above from one around it. */
  private readonly hostElement: ElementRef<HTMLElement> = inject(ElementRef);

  readonly header = input<string>();
  readonly ariaLabel = input<string>();
  readonly size = input<'sm' | 'md' | 'lg'>('md');
  readonly closeOnBackdropClick = input<boolean>(true);
  readonly closeOnEscape = input<boolean>(true);

  readonly closeEvent = output<void>();
  readonly isClosing = signal<boolean>(false);

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

  /**
   * Whether an open modal that does not contain `element` is stacked above it.
   *
   * A popup opened inside one modal (or on the page) must not be dismissed by clicks landing in a
   * dialog that was opened on top of it: the user is coming back to that popup once the dialog is
   * gone. Clicks inside the modal that merely contains the element are not covered by this.
   */
  static isStackedAbove(element: Element): boolean {
    return openModals.some((modal: AppModal): boolean => !modal.hostElement.nativeElement.contains(element));
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
    this.close();
  }

  close(): void {
    if (this.isClosing()) {
      return;
    }
    this.isClosing.set(true);
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
