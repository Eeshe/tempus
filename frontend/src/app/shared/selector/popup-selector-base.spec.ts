import { CUSTOM_ELEMENTS_SCHEMA, Component, Type } from '@angular/core';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { By } from '@angular/platform-browser';
import { AppModal } from '../modal/modal';
import { PopupSelectorBase } from './popup-selector-base';

const DEFAULT_TRIGGER_RECT: DOMRect = new DOMRect(50, 60, 80, 40);

@Component({
  selector: 'app-test-popup-selector',
  template: `
    <button #triggerButton type="button" (click)="toggle()">open</button>
    <div #popup class="selector__popup">popup</div>
  `,
})
class TestPopupSelector extends PopupSelectorBase {}

@Component({
  selector: 'app-anchored-test-popup-selector',
  template: `
    <button #triggerButton type="button" (click)="toggle()">open</button>
    <div #popup class="selector__popup">popup</div>
  `,
})
class AnchoredTestPopupSelector extends PopupSelectorBase {
  protected override readonly anchoredToViewport: boolean = true;
}

@Component({
  selector: 'app-right-placement-test-popup-selector',
  template: `
    <button #triggerButton type="button" (click)="toggle()">open</button>
    <div #popup class="selector__popup">popup</div>
  `,
})
class RightPlacementTestPopupSelector extends PopupSelectorBase {
  protected override readonly popupPlacement: 'below' | 'right' = 'right';
}

@Component({
  selector: 'app-carded-test-popup-selector',
  template: `
    <section class="card">
      <p class="card-title">Currency</p>
      <button #triggerButton type="button" (click)="toggle()">open</button>
      <div #popup class="selector__popup">popup</div>
    </section>
  `,
})
class CardedTestPopupSelector extends PopupSelectorBase {}

@Component({
  selector: 'app-page-host',
  imports: [TestPopupSelector],
  template: `
    <app-test-popup-selector />
  `,
})
class PageHost {}

@Component({
  selector: 'app-carded-host',
  imports: [CardedTestPopupSelector],
  template: `
    <p class="page-copy">page copy</p>
    <app-carded-test-popup-selector />
  `,
})
class CardedHost {}

@Component({
  selector: 'app-anchored-host',
  imports: [AnchoredTestPopupSelector],
  template: `
    <app-anchored-test-popup-selector />
  `,
})
class AnchoredHost {}

@Component({
  selector: 'app-right-placement-host',
  imports: [RightPlacementTestPopupSelector],
  template: `
    <app-right-placement-test-popup-selector />
  `,
})
class RightPlacementHost {}

@Component({
  selector: 'app-modal-host',
  imports: [AppModal, TestPopupSelector],
  template: `
    <app-modal>
      <app-test-popup-selector />
    </app-modal>
  `,
})
class ModalHost {}

@Component({
  selector: 'app-nav-host',
  imports: [TestPopupSelector],
  schemas: [CUSTOM_ELEMENTS_SCHEMA],
  template: `
    <app-navigation-bar>
      <app-test-popup-selector />
    </app-navigation-bar>
  `,
})
class NavHost {}

interface RenderedSelector {
  host: HTMLElement;
  popupElement: HTMLElement;
  component: PopupSelectorBase;
}

/** jsdom has no ResizeObserver; this stub lets the tests trigger the popup-resize path. */
class FakeResizeObserver {
  static instances: FakeResizeObserver[] = [];

  private readonly callback: ResizeObserverCallback;

  constructor(callback: ResizeObserverCallback) {
    this.callback = callback;
    FakeResizeObserver.instances.push(this);
  }

  observe(): void {}

  unobserve(): void {}

  disconnect(): void {}

  trigger(): void {
    this.callback([], this as unknown as ResizeObserver);
  }
}

function setScroll(scrollX: number, scrollY: number): void {
  Object.defineProperty(window, 'scrollX', { value: scrollX, configurable: true });
  Object.defineProperty(window, 'scrollY', { value: scrollY, configurable: true });
}

function setViewportHeight(height: number): void {
  Object.defineProperty(window, 'innerHeight', { value: height, configurable: true });
}

function setViewportWidth(width: number): void {
  Object.defineProperty(window, 'innerWidth', { value: width, configurable: true });
}

function scrollPage(): void {
  window.dispatchEvent(new Event('scroll'));
}

/** Locates the selector inside the fixture and pins its trigger rect to a known position. */
function findSelector(
  fixture: ComponentFixture<unknown>,
  directive: Type<PopupSelectorBase>,
  triggerRect: DOMRect = DEFAULT_TRIGGER_RECT,
): RenderedSelector {
  const debugElement = fixture.debugElement.query(By.directive(directive));
  const host: HTMLElement = debugElement.nativeElement as HTMLElement;
  const button: HTMLButtonElement | null = host.querySelector('button');
  const popupElement: HTMLElement | null = host.querySelector('.selector__popup');
  if (button == null || popupElement == null) {
    throw new Error('trigger button or popup not found');
  }
  button.getBoundingClientRect = (): DOMRect => triggerRect;
  return { host, popupElement, component: debugElement.componentInstance as PopupSelectorBase };
}

/** jsdom reports 0 for offsetWidth/offsetHeight, so popup sizes have to be stubbed. */
function setPopupSize(selector: RenderedSelector, width: number, height: number): void {
  Object.defineProperty(selector.popupElement, 'offsetWidth', { value: width, configurable: true });
  Object.defineProperty(selector.popupElement, 'offsetHeight', { value: height, configurable: true });
}

function scrollShift(host: HTMLElement): { x: string; y: string } {
  const style: CSSStyleDeclaration = host.style;
  return {
    x: style.getPropertyValue('--popup-scroll-x'),
    y: style.getPropertyValue('--popup-scroll-y'),
  };
}

describe('PopupSelectorBase', () => {
  let fixture: ComponentFixture<PageHost>;
  let selector: RenderedSelector;

  beforeEach(async () => {
    setScroll(0, 0);
    setViewportHeight(800);
    FakeResizeObserver.instances = [];
    Object.defineProperty(globalThis, 'ResizeObserver', { value: FakeResizeObserver, configurable: true });

    await TestBed.configureTestingModule({
      imports: [PageHost, AnchoredHost, ModalHost, NavHost, RightPlacementHost, CardedHost],
    }).compileComponents();

    fixture = TestBed.createComponent(PageHost);
    await fixture.whenStable();
    selector = findSelector(fixture, TestPopupSelector);
  });

  afterEach(() => {
    setScroll(0, 0);
    setViewportWidth(1024);
    Reflect.deleteProperty(globalThis, 'ResizeObserver');
  });

  it('spawns the popup just below the trigger', () => {
    setScroll(0, 120);

    selector.component.toggle();

    // rect.bottom (100) + 4px gap, in viewport coordinates.
    expect(selector.component.popupPosition()).toEqual({ top: 104, left: 50 });
    expect(selector.component.opensAbove()).toBe(false);
    expect(scrollShift(selector.host)).toEqual({ x: '0px', y: '0px' });
  });

  it('keeps the popup at its spawn point in the page while scrolling', () => {
    selector.component.toggle();

    setScroll(0, 260);
    scrollPage();

    // The fixed position is untouched, the popup is translated with the scrolled content.
    expect(selector.component.popupPosition()).toEqual({ top: 104, left: 50 });
    expect(scrollShift(selector.host)).toEqual({ x: '0px', y: '-260px' });
  });

  it('compensates horizontal scrolling too', () => {
    selector.component.toggle();

    setScroll(30, 0);
    scrollPage();

    expect(scrollShift(selector.host)).toEqual({ x: '-30px', y: '0px' });
  });

  it('ignores scroll once closed and starts from the new scroll offset when reopened', () => {
    selector.component.toggle();
    setScroll(0, 100);
    scrollPage();
    selector.component.toggle();

    setScroll(0, 500);
    scrollPage();
    expect(scrollShift(selector.host)).toEqual({ x: '0px', y: '-100px' });

    selector.component.toggle();
    expect(scrollShift(selector.host)).toEqual({ x: '0px', y: '0px' });

    setScroll(0, 560);
    scrollPage();
    expect(scrollShift(selector.host)).toEqual({ x: '0px', y: '-60px' });
  });

  it('flips the popup above the trigger when it does not fit below', () => {
    selector = findSelector(fixture, TestPopupSelector, new DOMRect(50, 700, 80, 40));
    setPopupSize(selector, 200, 300);

    selector.component.toggle();

    // Below would end at 740 + 4 + 300 > 800, above starts at 700 - 4 - 300.
    expect(selector.component.popupPosition()).toEqual({ top: 396, left: 50 });
    expect(selector.component.opensAbove()).toBe(true);
  });

  it('keeps the popup reachable when it fits on neither side', () => {
    setViewportHeight(400);
    selector = findSelector(fixture, TestPopupSelector, new DOMRect(50, 180, 80, 40));
    setPopupSize(selector, 200, 400);

    selector.component.toggle();

    // Neither 224 nor -224 fits in a 400px viewport, so the popup is pinned to the top edge.
    expect(selector.component.popupPosition()).toEqual({ top: 4, left: 50 });
  });

  it('re-places the popup when its content grows after opening', () => {
    selector = findSelector(fixture, TestPopupSelector, new DOMRect(50, 600, 80, 40));

    selector.component.toggle();
    expect(selector.component.popupPosition()).toEqual({ top: 644, left: 50 });

    // The list loads after the popup opened: it no longer fits below its trigger.
    setPopupSize(selector, 200, 300);
    FakeResizeObserver.instances[FakeResizeObserver.instances.length - 1].trigger();

    expect(selector.component.popupPosition()).toEqual({ top: 296, left: 50 });
    expect(selector.component.opensAbove()).toBe(true);
  });

  it('keeps the popup above when its content shrinks after opening above', () => {
    // Too tall to fit below a trigger close to the bottom, so the popup opens above it.
    selector = findSelector(fixture, TestPopupSelector, new DOMRect(50, 700, 80, 40));
    setPopupSize(selector, 200, 300);
    selector.component.toggle();
    expect(selector.component.opensAbove()).toBe(true);
    expect(selector.component.popupPosition()).toEqual({ top: 396, left: 50 });

    // Filtering the list down (the currency search) must not flip it across the trigger.
    setPopupSize(selector, 200, 60);
    FakeResizeObserver.instances[FakeResizeObserver.instances.length - 1].trigger();

    expect(selector.component.opensAbove()).toBe(true);
    // The bottom edge stays glued above the trigger: 700 - 4 - 60.
    expect(selector.component.popupPosition()).toEqual({ top: 636, left: 50 });
  });

  it('keeps the popup below when its content shrinks after opening below', () => {
    selector = findSelector(fixture, TestPopupSelector, new DOMRect(50, 60, 80, 40));
    setPopupSize(selector, 200, 300);
    selector.component.toggle();
    expect(selector.component.opensAbove()).toBe(false);
    expect(selector.component.popupPosition()).toEqual({ top: 104, left: 50 });

    setPopupSize(selector, 200, 60);
    FakeResizeObserver.instances[FakeResizeObserver.instances.length - 1].trigger();

    expect(selector.component.opensAbove()).toBe(false);
    expect(selector.component.popupPosition()).toEqual({ top: 104, left: 50 });
  });

  it('decides the side again on the next opening', () => {
    // 300px does not fit below this trigger (156px of room), 60px does.
    selector = findSelector(fixture, TestPopupSelector, new DOMRect(50, 600, 80, 40));
    setPopupSize(selector, 200, 300);
    selector.component.toggle();
    expect(selector.component.opensAbove()).toBe(true);

    selector.component.toggle();
    setPopupSize(selector, 200, 60);
    selector.component.toggle();

    expect(selector.component.opensAbove()).toBe(false);
    expect(selector.component.popupPosition()).toEqual({ top: 644, left: 50 });
  });

  it('never places the popup under the sticky nav bar', async () => {
    const navFixture: ComponentFixture<NavHost> = TestBed.createComponent(NavHost);
    await navFixture.whenStable();
    const navBar: HTMLElement | null = navFixture.nativeElement.querySelector('app-navigation-bar');
    if (navBar == null) {
      throw new Error('navigation bar not found');
    }
    navBar.getBoundingClientRect = (): DOMRect => new DOMRect(0, 0, 1000, 60);
    const navSelector: RenderedSelector = findSelector(navFixture, TestPopupSelector, new DOMRect(50, 700, 80, 40));
    // Taller than the whole area below the nav, so the clamp decides where it ends up.
    setPopupSize(navSelector, 200, 760);

    navSelector.component.toggle();

    // Pinned below the nav (60 + 4) instead of at the top of the viewport, where its first rows
    // would be painted underneath the nav. setPopupSize does not fire the observer, so the
    // placement below is the one decided while opening.
    expect(navSelector.component.popupPosition()).toEqual({ top: 64, left: 50 });
    expect(navSelector.component.opensAbove()).toBe(true);
  });

  it('always places the popup beside the trigger when its placement is right', async () => {
    const rightFixture: ComponentFixture<RightPlacementHost> = TestBed.createComponent(RightPlacementHost);
    await rightFixture.whenStable();
    // Plenty of room below, but a right placement ignores that and stays beside the trigger.
    const rightSelector: RenderedSelector = findSelector(
      rightFixture,
      RightPlacementTestPopupSelector,
      new DOMRect(50, 60, 80, 40),
    );
    setPopupSize(rightSelector, 200, 100);

    rightSelector.component.toggle();

    // Trigger right edge (130) + 4px gap, top aligned with the trigger.
    expect(rightSelector.component.popupPosition()).toEqual({ top: 60, left: 134 });
    expect(rightSelector.component.opensAbove()).toBe(false);
  });

  it('pushes a right placed popup up just enough to keep it on screen', async () => {
    const rightFixture: ComponentFixture<RightPlacementHost> = TestBed.createComponent(RightPlacementHost);
    await rightFixture.whenStable();
    const rightSelector: RenderedSelector = findSelector(
      rightFixture,
      RightPlacementTestPopupSelector,
      new DOMRect(50, 700, 80, 40),
    );
    setPopupSize(rightSelector, 200, 300);

    rightSelector.component.toggle();

    // Still beside the trigger, but shifted up so its 300px height stays inside the viewport.
    expect(rightSelector.component.popupPosition()).toEqual({ top: 496, left: 134 });
    expect(rightSelector.component.opensAbove()).toBe(false);
  });

  it('keeps a right placed popup inside a narrow viewport', async () => {
    const rightFixture: ComponentFixture<RightPlacementHost> = TestBed.createComponent(RightPlacementHost);
    await rightFixture.whenStable();
    setViewportWidth(300);
    const rightSelector: RenderedSelector = findSelector(
      rightFixture,
      RightPlacementTestPopupSelector,
      new DOMRect(50, 400, 80, 40),
    );
    setPopupSize(rightSelector, 200, 100);

    rightSelector.component.toggle();

    // 130 + 4 + 200 > 300, so the popup is pulled inside the viewport instead of flipping above.
    expect(rightSelector.component.popupPosition()).toEqual({ top: 400, left: 96 });
    expect(rightSelector.component.opensAbove()).toBe(false);
  });

  it('stays viewport anchored when the component opts in', async () => {
    const anchoredFixture: ComponentFixture<AnchoredHost> = TestBed.createComponent(AnchoredHost);
    await anchoredFixture.whenStable();
    const anchoredSelector: RenderedSelector = findSelector(anchoredFixture, AnchoredTestPopupSelector);

    anchoredSelector.component.toggle();
    setScroll(0, 300);
    scrollPage();

    expect(scrollShift(anchoredSelector.host)).toEqual({ x: '0px', y: '0px' });
  });

  it('stays viewport anchored inside a modal', async () => {
    const modalFixture: ComponentFixture<ModalHost> = TestBed.createComponent(ModalHost);
    await modalFixture.whenStable();
    const modalSelector: RenderedSelector = findSelector(modalFixture, TestPopupSelector);

    modalSelector.component.toggle();
    setScroll(0, 300);
    scrollPage();

    expect(scrollShift(modalSelector.host)).toEqual({ x: '0px', y: '0px' });
  });

  describe('search term lifecycle', () => {
    it('keeps the search term while the popup closes', () => {
      selector.component.toggle();
      selector.component.searchQuery.set('eur');

      selector.component.toggle();

      // Clearing it here would flash the unfiltered list during the closing transition.
      expect(selector.component.searchQuery()).toBe('eur');
    });

    it('starts every opening with an empty search term', () => {
      selector.component.searchQuery.set('eur');

      selector.component.toggle();

      expect(selector.component.searchQuery()).toBe('');
    });
  });

  describe('closing on document clicks', () => {
    let cardedFixture: ComponentFixture<CardedHost>;
    let cardedSelector: RenderedSelector;

    beforeEach(async () => {
      cardedFixture = TestBed.createComponent(CardedHost);
      await cardedFixture.whenStable();
      cardedSelector = findSelector(cardedFixture, CardedTestPopupSelector);
      cardedSelector.component.toggle();
    });

    function click(element: Element | null): void {
      if (element == null) {
        throw new Error('element to click not found');
      }
      (element as HTMLElement).click();
    }

    it('closes when a sibling inside the host element is clicked', () => {
      click(cardedSelector.host.querySelector('.card-title'));

      expect(cardedSelector.component.isOpen()).toBe(false);
    });

    it('stays open when the popup itself is clicked', () => {
      click(cardedSelector.popupElement);

      expect(cardedSelector.component.isOpen()).toBe(true);
    });

    it('closes when the page around the host element is clicked', () => {
      click(cardedFixture.nativeElement.querySelector('.page-copy'));

      expect(cardedSelector.component.isOpen()).toBe(false);
    });

    it('toggles once when the trigger is clicked', () => {
      click(cardedSelector.host.querySelector('button'));
      expect(cardedSelector.component.isOpen()).toBe(false);

      click(cardedSelector.host.querySelector('button'));
      expect(cardedSelector.component.isOpen()).toBe(true);
    });
  });
});
