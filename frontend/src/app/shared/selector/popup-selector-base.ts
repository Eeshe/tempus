import { Directive, ElementRef, HostListener, inject, signal, viewChild } from '@angular/core';

@Directive({
  standalone: true,
})
export abstract class PopupSelectorBase {
  protected readonly hostElement: ElementRef = inject(ElementRef);
  protected readonly triggerButton = viewChild<ElementRef<HTMLButtonElement>>('triggerButton');
  protected readonly popup = viewChild<ElementRef<HTMLDivElement>>('popup');
  protected readonly alignPopupRight: boolean = false;

  readonly isOpen = signal<boolean>(false);
  readonly popupPosition = signal<{ top: number; left: number }>({ top: 0, left: 0 });

  toggle(): void {
    if (!this.isOpen()) {
      this.openPopup();
    }
    this.isOpen.update((isOpen: boolean) => !isOpen);
  }

  protected openPopup(): void {
    const button: HTMLButtonElement | undefined = this.triggerButton()?.nativeElement;
    if (!button) {
      return;
    }
    const rect: DOMRect = button.getBoundingClientRect();
    const width: number = this.popup()?.nativeElement.offsetWidth ?? 0;
    const left: number = this.alignPopupRight ? rect.right - width : rect.left;
    this.popupPosition.set({ top: rect.bottom + 4, left });
  }

  @HostListener('document:click', ['$event'])
  onDocumentClick(event: MouseEvent): void {
    if (!this.isOpen() || this.hostElement.nativeElement.contains(event.target)) {
      return;
    }
    this.isOpen.set(false);
  }
}
