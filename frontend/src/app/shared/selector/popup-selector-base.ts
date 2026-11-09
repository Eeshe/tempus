import { DestroyRef, Directive, ElementRef, HostListener, inject, signal, viewChild } from '@angular/core';

/** Distance between the trigger and the popup, on whichever side the popup is placed. */
const POPUP_GAP: number = 4;

/** Side of the trigger a popup opens on. */
export type PopupPlacement = 'below' | 'right';

/** Viewport box the trigger occupied when the popup was opened. */
interface SpawnRect {
  top: number;
  bottom: number;
  left: number;
  right: number;
}

@Directive({
  standalone: true,
})
export abstract class PopupSelectorBase {
  private readonly destroyRef: DestroyRef = inject(DestroyRef);

  protected readonly hostElement: ElementRef = inject(ElementRef);
  protected readonly triggerButton = viewChild<ElementRef<HTMLButtonElement>>('triggerButton');
  protected readonly popup = viewChild<ElementRef<HTMLDivElement>>('popup');
  protected readonly alignPopupRight: boolean = false;

  protected readonly popupPlacement: PopupPlacement = 'below';

  /**
   * Set this in components whose popup must stay glued to the viewport instead of scrolling away
   * with the page (e.g. triggers inside the sticky nav bar).
   */
  protected readonly anchoredToViewport: boolean = false;

  readonly isOpen = signal<boolean>(false);
  readonly popupPosition = signal<{ top: number; left: number }>({ top: 0, left: 0 });
  readonly opensAbove = signal<boolean>(false);
  readonly searchQuery = signal<string>('');

  private spawnViewportRect: SpawnRect | null = null;

  private scrollOriginY: number | null = null;
  private scrollOriginX: number | null = null;

  private appliedShiftY: number = 0;
  private appliedShiftX: number = 0;

  /** Resolved anchoring, decided when the popup opens (explicit flag or modal host). */
  private anchored: boolean = false;

  private popupResizeObserver: ResizeObserver | null = null;

  constructor() {
    const onScroll: () => void = (): void => this.syncPopupScrollShift();

    // Passive so the page never waits on this handler.
    window.addEventListener('scroll', onScroll, { passive: true });
    this.destroyRef.onDestroy(() => {
      window.removeEventListener('scroll', onScroll);
      this.stopPopupResizeTracking();
    });
  }

  private syncPopupScrollShift(): void {
    const originY: number | null = this.scrollOriginY;
    const originX: number | null = this.scrollOriginX;
    if (!this.isOpen() || originY == null || originX == null || this.anchored) {
      return;
    }
    const shiftY: number = originY - window.scrollY;
    const shiftX: number = originX - window.scrollX;
    if (shiftY === this.appliedShiftY && shiftX === this.appliedShiftX) {
      return;
    }
    this.appliedShiftY = shiftY;
    this.appliedShiftX = shiftX;
    this.writeScrollShift(shiftX, shiftY);
  }

  toggle(): void {
    if (this.isOpen()) {
      this.closeSelector();
      return;
    }
    this.openPopup();
    this.isOpen.set(true);
  }

  protected closeSelector(): void {
    this.isOpen.set(false);
    this.searchQuery.set('');

    // The current shift is deliberately left in place: the closing transition should fade the
    // popup out where it currently is, not jump back to its spawn point first.
    this.scrollOriginY = null;
    this.scrollOriginX = null;
    this.spawnViewportRect = null;
    this.stopPopupResizeTracking();
  }

  private stopPopupResizeTracking(): void {
    this.popupResizeObserver?.disconnect();
  }

  protected openPopup(): void {
    const button: HTMLButtonElement | undefined = this.triggerButton()?.nativeElement;
    if (!button) {
      return;
    }
    const rect: DOMRect = button.getBoundingClientRect();
    this.spawnViewportRect = {
      top: rect.top,
      bottom: rect.bottom,
      left: rect.left,
      right: rect.right,
    };
    this.anchored = this.isAnchoredToViewport();
    this.scrollOriginY = window.scrollY;
    this.scrollOriginX = window.scrollX;
    this.appliedShiftY = 0;
    this.appliedShiftX = 0;
    this.writeScrollShift(0, 0);

    this.startPopupResizeTracking();
    this.placePopup();
  }

  /**
   * Watches the open popup for size changes so `placePopup()` can re-decide between below/above once
   * an asynchronously loaded list (or a search filter) changes the popup's height.
   */
  private startPopupResizeTracking(): void {
    const popupElement: HTMLDivElement | undefined = this.popup()?.nativeElement;
    if (popupElement == null || typeof ResizeObserver === 'undefined') {
      return;
    }
    this.popupResizeObserver ??= new ResizeObserver((): void => {
      if (!this.isOpen()) {
        return;
      }
      this.placePopup();
    });
    this.popupResizeObserver.disconnect();
    this.popupResizeObserver.observe(popupElement);
  }

  protected matchesSearch(value: string): boolean {
    const query: string = this.searchQuery().trim().toLowerCase();
    return query === '' || value.toLowerCase().includes(query);
  }

  /**
   * Written straight to the host's inline style instead of a bound signal: this runs on every
   * scroll frame and must not trigger change detection (that is what makes popups look jittery).
   * The popup inherits the custom properties, `selector-shared.css` consumes them in `translate`.
   */
  private writeScrollShift(shiftX: number, shiftY: number): void {
    const style: CSSStyleDeclaration = this.hostElement.nativeElement.style;
    style.setProperty('--popup-scroll-x', `${shiftX}px`);
    style.setProperty('--popup-scroll-y', `${shiftY}px`);
  }

  /**
   * Re-run whenever the popup resizes (lists load asynchronously, search filtering shrinks them).
   */
  private placePopup(): void {
    const spawn: SpawnRect | null = this.spawnViewportRect;
    if (spawn == null) {
      return;
    }
    const popupElement: HTMLDivElement | undefined = this.popup()?.nativeElement;
    const width: number = popupElement?.offsetWidth ?? 0;
    const height: number = popupElement?.offsetHeight ?? 0;
    const topBoundary: number = this.calculatePopupTopBoundary();
    const maxTop: number = window.innerHeight - POPUP_GAP - height;
    if (this.popupPlacement === 'right') {
      this.setOpensAbove(false);
      // Beside the trigger and top aligned with it, pushed up/in only as far as needed to stay on
      // screen: the trigger itself is never covered.
      this.setPopupPosition(
        Math.min(spawn.right + POPUP_GAP, window.innerWidth - POPUP_GAP - width),
        Math.max(topBoundary, Math.min(spawn.top, maxTop)),
      );
      return;
    }
    const belowTop: number = spawn.bottom + POPUP_GAP;
    const aboveTop: number = spawn.top - POPUP_GAP - height;
    const spaceBelow: number = window.innerHeight - belowTop;
    const spaceAbove: number = spawn.top - POPUP_GAP - topBoundary;
    const fitsBelow: boolean = height <= spaceBelow;
    const fitsAbove: boolean = height <= spaceAbove;

    // When neither side fits, use the roomier one so the popup stays as reachable as possible.
    const placeAbove: boolean = !fitsBelow && (fitsAbove || spaceAbove > spaceBelow);
    this.setOpensAbove(placeAbove);

    // Clamped into the area between the sticky nav bar and the bottom of the viewport: a popup that
    // is taller than that area is pinned below the nav instead of having its top hidden under it.
    this.setPopupPosition(
      this.alignPopupRight ? spawn.right - width : spawn.left,
      Math.max(topBoundary, Math.min(placeAbove ? aboveTop : belowTop, maxTop)),
    );
  }

  private setPopupPosition(left: number, top: number): void {
    const current: { top: number; left: number } = this.popupPosition();
    if (current.top === top && current.left === left) {
      return;
    }
    this.popupPosition.set({ top, left });
  }

  private calculatePopupTopBoundary(): number {
    // The navigation bar host, not a bare `nav` tag: the settings page has its own `nav` sidebar.
    const navBar: HTMLElement | null = document.querySelector<HTMLElement>('app-navigation-bar');
    const navBottom: number = navBar?.getBoundingClientRect().bottom ?? 0;

    return navBottom + POPUP_GAP;
  }

  private setOpensAbove(opensAbove: boolean): void {
    if (this.opensAbove() === opensAbove) {
      return;
    }
    this.opensAbove.set(opensAbove);
  }

  /**
   * Whether the popup follows the viewport. Modals are detected through their host element: the
   * modal backdrop is `position: fixed` and modal content is projected inside `<app-modal>`, so a
   * popup in there must not slide away when the page scrolls behind the dialog.
   */
  private isAnchoredToViewport(): boolean {
    const host: HTMLElement = this.hostElement.nativeElement as HTMLElement;

    return this.anchoredToViewport || host.closest('app-modal') != null;
  }

  @HostListener('document:click', ['$event'])
  onDocumentClick(event: MouseEvent): void {
    if (!this.isOpen() || this.hostElement.nativeElement.contains(event.target)) {
      return;
    }
    this.closeSelector();
  }
}
