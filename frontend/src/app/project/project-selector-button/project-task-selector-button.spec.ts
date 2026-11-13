import { Component, signal } from '@angular/core';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { By } from '@angular/platform-browser';
import { of } from 'rxjs';
import { ClientService } from '../../client/services/client.service';
import { CurrencyService } from '../../services/currency.service';
import { ProjectService } from '../../services/project.service';
import { AppModal } from '../../shared/modal/modal';
import { ProjectTaskSelectorButton } from './project-task-selector-button';

/** Distance between the trigger and the popup, mirrored from `PopupSelectorBase`. */
const POPUP_GAP: number = 4;

/** Trigger box used in every test: `left` 50, `right` 130, `top` 60, `bottom` 100. */
const TRIGGER_RECT: DOMRect = new DOMRect(50, 60, 80, 40);

@Component({
  selector: 'app-modal-selector-host',
  imports: [AppModal, ProjectTaskSelectorButton],
  template: `
    <app-modal header="New Time Entry">
      <app-project-task-selector-button placement="below" />
    </app-modal>
  `,
})
class ModalSelectorHost {}

interface RenderedSelector {
  host: HTMLElement;
  component: ProjectTaskSelectorButton;
}

/** Locates the selector, pins its trigger rect and stubs the popup size (jsdom reports 0s). */
function findSelector(fixture: ComponentFixture<unknown>): RenderedSelector {
  // A fixture created for the selector itself has its own debug element as the root.
  const debugElement =
    fixture.debugElement.query(By.directive(ProjectTaskSelectorButton)) ?? fixture.debugElement;
  const host: HTMLElement = debugElement.nativeElement as HTMLElement;
  const trigger: HTMLButtonElement | null = host.querySelector('button');
  const popupElement: HTMLElement | null = host.querySelector('.selector__popup');
  if (trigger == null || popupElement == null) {
    throw new Error('trigger button or popup not found');
  }
  trigger.getBoundingClientRect = (): DOMRect => TRIGGER_RECT;
  Object.defineProperty(popupElement, 'offsetWidth', { value: 200, configurable: true });
  Object.defineProperty(popupElement, 'offsetHeight', { value: 100, configurable: true });

  return { host, component: debugElement.componentInstance as ProjectTaskSelectorButton };
}

describe('ProjectTaskSelectorButton', () => {
  let fixture: ComponentFixture<ProjectTaskSelectorButton>;
  let component: ProjectTaskSelectorButton;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [ProjectTaskSelectorButton],
      // No HTTP in these tests: lists are only fetched when a popup opens.
      providers: [
        {
          provide: ProjectService,
          useValue: { listProjects: () => of([]), createProject: () => of({}) },
        },
        { provide: ClientService, useValue: { listClients: () => of([]) } },
        { provide: CurrencyService, useValue: { symbol: signal('$') } },
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(ProjectTaskSelectorButton);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('opens beside the trigger by default', () => {
    const selector: RenderedSelector = findSelector(fixture);

    selector.component.toggle();

    // Trigger right edge (130) + gap, top aligned with the trigger.
    expect(selector.component.popupPosition()).toEqual({ top: TRIGGER_RECT.top, left: 130 + POPUP_GAP });
    expect(selector.component.opensAbove()).toBe(false);
  });

  it('opens below the trigger when the placement input says so', async () => {
    fixture.componentRef.setInput('placement', 'below');
    await fixture.whenStable();
    const selector: RenderedSelector = findSelector(fixture);

    selector.component.toggle();

    // Trigger bottom (100) + gap, left aligned with the trigger.
    expect(selector.component.popupPosition()).toEqual({ top: TRIGGER_RECT.bottom + POPUP_GAP, left: TRIGGER_RECT.left });
    expect(selector.component.opensAbove()).toBe(false);
  });

  it('re-reads the placement input on the next opening', () => {
    const selector: RenderedSelector = findSelector(fixture);
    selector.component.toggle();
    expect(selector.component.popupPosition()).toEqual({ top: TRIGGER_RECT.top, left: 130 + POPUP_GAP });

    fixture.componentRef.setInput('placement', 'below');
    selector.component.toggle();
    selector.component.toggle();

    expect(selector.component.popupPosition()).toEqual({ top: TRIGGER_RECT.bottom + POPUP_GAP, left: TRIGGER_RECT.left });
  });

  it('keeps the popup open while the create-project dialog it opened is in use', async () => {
    const selector: RenderedSelector = findSelector(fixture);
    selector.component.toggle();

    const newProjectButton: HTMLButtonElement | null = selector.host.querySelector(
      '.selector__popup > .popup__new',
    );
    newProjectButton?.click();
    await fixture.whenStable();

    expect(selector.host.querySelector('app-create-project-form-modal app-modal')).not.toBeNull();
    expect(selector.component.isOpen()).toBe(true);

    // Typing in the dialog is a document click, but a dialog sits above the popup now.
    (selector.host.querySelector('#name') as HTMLInputElement | null)?.click();
    await fixture.whenStable();

    expect(selector.component.isOpen()).toBe(true);
  });

  it('keeps a below placed popup glued to the viewport inside a modal', async () => {
    const modalFixture: ComponentFixture<ModalSelectorHost> = TestBed.createComponent(ModalSelectorHost);
    await modalFixture.whenStable();
    const selector: RenderedSelector = findSelector(modalFixture);

    selector.component.toggle();
    expect(selector.component.popupPosition()).toEqual({ top: TRIGGER_RECT.bottom + POPUP_GAP, left: TRIGGER_RECT.left });

    // Scrolling the page behind the dialog must not move the popup.
    Object.defineProperty(window, 'scrollY', { value: 250, configurable: true });
    window.dispatchEvent(new Event('scroll'));

    expect(selector.host.style.getPropertyValue('--popup-scroll-y')).toBe('0px');
    Object.defineProperty(window, 'scrollY', { value: 0, configurable: true });
  });
});
