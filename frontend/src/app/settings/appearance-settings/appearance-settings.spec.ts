import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { AuthService } from '../../services/auth.service';
import { DEFAULT_THEME, THEME_STORAGE_KEY } from '../../services/theme.service';
import { DEFAULT_CURRENCY } from '../../shared/util/currency.util';
import type { Theme, User } from '../../user/models/user.model';
import { AppearanceSettings } from './appearance-settings';

const SAVED_THEME: Theme = {
  background: '#101318',
  surface: '#161a22',
  text: '#e8eaf0',
  accent: '#3dd68c',
  danger: '#f2555c',
};

function createUser(theme: Theme = SAVED_THEME): User {
  return {
    id: 1,
    name: 'tester',
    settings: { theme, currency: DEFAULT_CURRENCY },
    createdAt: '2026-01-01T00:00:00Z',
  };
}

function readVariable(name: string): string {
  return document.documentElement.style.getPropertyValue(name);
}

function clearVariables(): void {
  for (const name of ['--bg', '--surface', '--fg', '--good', '--bad']) {
    document.documentElement.style.removeProperty(name);
  }
}

describe('AppearanceSettings', () => {
  let fixture: ComponentFixture<AppearanceSettings>;
  let component: AppearanceSettings;
  let httpMock: HttpTestingController;
  let authService: AuthService;

  beforeEach(() => {
    localStorage.clear();
    clearVariables();
    TestBed.configureTestingModule({
      providers: [provideHttpClient(), provideHttpClientTesting()],
    });
    httpMock = TestBed.inject(HttpTestingController);
    authService = TestBed.inject(AuthService);

    authService.checkAuthentication().subscribe();
    httpMock.expectOne('/api/v1/auth/me').flush(createUser());

    fixture = TestBed.createComponent(AppearanceSettings);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  afterEach(() => {
    httpMock.verify();
    localStorage.clear();
    clearVariables();
  });

  function button(selector: string): HTMLButtonElement {
    return fixture.nativeElement.querySelector(selector) as HTMLButtonElement;
  }

  it('renders one color input per theme field with the saved colors', () => {
    const hexValues: string[] = Array.from(
      fixture.nativeElement.querySelectorAll('.color-input__hex') as NodeListOf<HTMLElement>,
    ).map((element: HTMLElement) => element.textContent?.trim() ?? '');

    expect(hexValues).toEqual(['#101318', '#161A22', '#E8EAF0', '#3DD68C', '#F2555C']);
  });

  it('previews color changes instantly and enables apply', () => {
    expect(button('.appearance__button--primary').disabled).toBe(true);

    component.updateColor('background', '#123456');
    fixture.detectChanges();

    expect(readVariable('--bg')).toBe('#123456');
    expect(component.isDirty()).toBe(true);
    expect(button('.appearance__button--primary').disabled).toBe(false);
    // A draft must not become the palette painted on the next first paint.
    expect(JSON.parse(localStorage.getItem(THEME_STORAGE_KEY)!)).toEqual(SAVED_THEME);
  });

  it('patches the theme, marks it saved and keeps the preview without reloading', () => {
    component.updateColor('accent', '#00ff00');
    fixture.detectChanges();

    button('.appearance__button--primary').click();

    const request = httpMock.expectOne('/api/v1/settings');
    expect(request.request.body).toEqual({ theme: component.draft() });
    request.flush({ theme: component.draft() });
    fixture.detectChanges();

    expect(authService.currentUser()?.settings.theme.accent).toBe('#00ff00');
    expect(component.isDirty()).toBe(false);
    expect(button('.appearance__button--primary').disabled).toBe(true);
    expect(readVariable('--good')).toBe('#00ff00');
  });

  it('shows an error and keeps the draft when saving fails', () => {
    component.updateColor('danger', '#990000');
    fixture.detectChanges();

    component.apply();
    httpMock.expectOne('/api/v1/settings').flush({}, { status: 500, statusText: 'Server Error' });
    fixture.detectChanges();

    expect(component.saveError()).toBeTruthy();
    expect(fixture.nativeElement.querySelector('.appearance__error')).toBeTruthy();
    expect(component.draft().danger).toBe('#990000');
    expect(component.isDirty()).toBe(true);
    expect(component.isSaving()).toBe(false);
  });

  it('resets the draft to the saved theme', () => {
    component.updateColor('surface', '#222222');
    fixture.detectChanges();

    button('.appearance__button--secondary').click();
    fixture.detectChanges();

    expect(component.draft()).toEqual(SAVED_THEME);
    expect(component.isDirty()).toBe(false);
    expect(readVariable('--surface')).toBe(SAVED_THEME.surface);
  });

  it('restores the default palette without saving', () => {
    component.updateColor('text', '#ffffff');
    fixture.detectChanges();

    const restoreButton: HTMLButtonElement = Array.from(
      fixture.nativeElement.querySelectorAll(
        '.appearance__button--secondary',
      ) as NodeListOf<HTMLButtonElement>,
    )[1];
    restoreButton.click();
    fixture.detectChanges();

    expect(component.draft()).toEqual(DEFAULT_THEME);
    expect(component.isDefaultTheme()).toBe(true);
    expect(component.isDirty()).toBe(false);
    expect(readVariable('--fg')).toBe(DEFAULT_THEME.text);
  });

  it('resolves the discard confirmation when leaving with unsaved changes', async () => {
    component.updateColor('background', '#abcdef');
    fixture.detectChanges();

    const discardPromise: Promise<boolean> = component.confirmDiscard();
    fixture.detectChanges();
    expect(fixture.nativeElement.querySelector('app-delete-confirmation-modal')).toBeTruthy();

    component.handleDiscardConfirmation(true);

    await expect(discardPromise).resolves.toBe(true);
    expect(component.isDirty()).toBe(false);
    expect(readVariable('--bg')).toBe(SAVED_THEME.background);
  });

  it('keeps the draft when the discard confirmation is cancelled', async () => {
    component.updateColor('background', '#abcdef');
    fixture.detectChanges();

    const discardPromise: Promise<boolean> = component.confirmDiscard();
    component.handleDiscardConfirmation(false);

    await expect(discardPromise).resolves.toBe(false);
    expect(component.isDirty()).toBe(true);
    expect(readVariable('--bg')).toBe('#abcdef');
  });

  it('warns about unsaved changes when the tab is closed', () => {
    const event = new Event('beforeunload', { cancelable: true }) as BeforeUnloadEvent;

    component.warnAboutUnsavedChanges(event);

    expect(event.defaultPrevented).toBe(false);

    component.updateColor('text', '#eeeeee');
    component.warnAboutUnsavedChanges(event);

    expect(event.defaultPrevented).toBe(true);
  });
});
