import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { AuthService } from '../../services/auth.service';
import { DEFAULT_THEME } from '../../services/theme.service';
import { getCurrencyOptions } from '../../shared/util/currency.util';
import type { User } from '../../user/models/user.model';
import { CurrencySettings } from './currency-settings';

function createUser(currency: string = 'USD'): User {
  return {
    id: 1,
    name: 'tester',
    settings: { theme: DEFAULT_THEME, currency },
    createdAt: '2026-01-01T00:00:00Z',
  };
}

describe('CurrencySettings', () => {
  let fixture: ComponentFixture<CurrencySettings>;
  let component: CurrencySettings;
  let httpMock: HttpTestingController;
  let authService: AuthService;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [CurrencySettings],
      providers: [provideHttpClient(), provideHttpClientTesting()],
    }).compileComponents();

    httpMock = TestBed.inject(HttpTestingController);
    authService = TestBed.inject(AuthService);

    authService.login('tester', 'password').subscribe();
    httpMock.expectOne('/api/v1/auth/login').flush(createUser());

    fixture = TestBed.createComponent(CurrencySettings);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  afterEach(() => {
    httpMock.verify();
  });

  function renderedCodes(): string[] {
    const options: HTMLElement[] = Array.from(
      fixture.nativeElement.querySelectorAll('.selector__popup-code'),
    );

    return options.map((option) => option.textContent!.trim());
  }

  async function typeSearch(query: string): Promise<void> {
    const input: HTMLInputElement = fixture.nativeElement.querySelector('.search-input');

    input.value = query;
    input.dispatchEvent(new Event('input'));
    await fixture.whenStable();
  }

  it('shows the configured currency and its symbol', () => {
    const trigger: HTMLElement = fixture.nativeElement.querySelector('.selector__trigger');

    expect(trigger.textContent).toContain('USD');
    expect(trigger.textContent).toContain('$');
  });

  it('lists every currency code alphabetically', () => {
    const codes: string[] = renderedCodes();
    const expectedCodes: string[] = getCurrencyOptions('en-US').map((option) => option.code);

    expect(codes).toEqual(expectedCodes);
    expect(codes.length).toBeGreaterThan(0);
    expect([...codes].sort((first, second) => first.localeCompare(second))).toEqual(codes);
  });

  it('labels every entry with its currency name', () => {
    const wrapper: HTMLElement = fixture.nativeElement.querySelector('.selector__popup-list');
    const names: HTMLElement[] = Array.from(wrapper.querySelectorAll('.selector__popup-name'));

    expect(names.length).toBe(renderedCodes().length);
    expect(names[0].textContent!.trim()).not.toBe('');
  });

  it('saves the selected currency right away', async () => {
    const euroOption: HTMLButtonElement = Array.from<HTMLButtonElement>(
      fixture.nativeElement.querySelectorAll('.selector__popup-option'),
    ).find((option) => option.textContent!.includes('EUR'))!;

    euroOption.click();

    const request = httpMock.expectOne('/api/v1/settings');
    expect(request.request.method).toBe('PATCH');
    expect(request.request.body).toEqual({ currency: 'EUR' });
    request.flush({ theme: DEFAULT_THEME, currency: 'EUR' });

    await fixture.whenStable();

    expect(authService.currentUser()?.settings.currency).toBe('EUR');
    expect(component.currency()).toBe('EUR');
    expect(fixture.nativeElement.querySelector('.selector__trigger').textContent).toContain('EUR');
  });

  it('filters the currencies by code', async () => {
    await typeSearch('eur');

    expect(renderedCodes()).toEqual(['EUR']);
  });

  it('filters the currencies by name', async () => {
    await typeSearch('japanese');

    expect(renderedCodes()).toEqual(['JPY']);
  });

  it('ignores the casing of the query', async () => {
    await typeSearch('YeN');

    expect(renderedCodes()).toEqual(['JPY']);
  });

  it('renders no rows when nothing matches', async () => {
    await typeSearch('zzzz');

    expect(renderedCodes()).toEqual([]);
  });

  it('keeps the filtered list while closing and restores it when reopened', async () => {
    const trigger: HTMLButtonElement = fixture.nativeElement.querySelector('.selector__trigger');

    trigger.click();
    await typeSearch('eur');
    expect(renderedCodes()).toEqual(['EUR']);

    // The closing fade keeps showing the filtered list, so the full one does not flash by.
    trigger.click();
    await fixture.whenStable();
    expect(renderedCodes()).toEqual(['EUR']);

    trigger.click();
    await fixture.whenStable();
    expect(renderedCodes()).toEqual(getCurrencyOptions('en-US').map((option) => option.code));
  });

  it('closes when the card around the selector is clicked', async () => {
    const trigger: HTMLButtonElement = fixture.nativeElement.querySelector('.selector__trigger');

    trigger.click();
    await fixture.whenStable();
    expect(component.isOpen()).toBe(true);

    (fixture.nativeElement.querySelector('.currency__title') as HTMLElement).click();
    await fixture.whenStable();

    expect(component.isOpen()).toBe(false);
  });

  it('stays open when the search field is clicked', async () => {
    const trigger: HTMLButtonElement = fixture.nativeElement.querySelector('.selector__trigger');

    trigger.click();
    await fixture.whenStable();

    (fixture.nativeElement.querySelector('.search-input') as HTMLElement).click();
    await fixture.whenStable();

    expect(component.isOpen()).toBe(true);
  });

  it('does not request a patch when the current currency is selected again', () => {
    const usdOption: HTMLButtonElement = Array.from<HTMLButtonElement>(
      fixture.nativeElement.querySelectorAll('.selector__popup-option'),
    ).find((option) => option.textContent!.includes('USD'))!;

    usdOption.click();

    httpMock.expectNone('/api/v1/settings');
  });

  it('shows an error when the patch fails', async () => {
    const euroOption: HTMLButtonElement = Array.from<HTMLButtonElement>(
      fixture.nativeElement.querySelectorAll('.selector__popup-option'),
    ).find((option) => option.textContent!.includes('EUR'))!;

    euroOption.click();
    httpMock
      .expectOne('/api/v1/settings')
      .flush({}, { status: 500, statusText: 'Internal Server Error' });
    await fixture.whenStable();

    expect(fixture.nativeElement.querySelector('.currency__error')).toBeTruthy();
    expect(authService.currentUser()?.settings.currency).toBe('USD');
  });
});
