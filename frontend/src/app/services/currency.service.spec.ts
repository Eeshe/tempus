import { registerLocaleData } from '@angular/common';
import localeEs419 from '@angular/common/locales/es-419';
import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { LOCALE_ID } from '@angular/core';
import { TestBed } from '@angular/core/testing';
import { DEFAULT_THEME } from './theme.service';
import { CurrencyService } from './currency.service';
import { AuthService } from './auth.service';
import type { User } from '../user/models/user.model';

registerLocaleData(localeEs419, 'es-419');

function createUser(currency: string = 'EUR'): User {
  return {
    id: 1,
    name: 'tester',
    settings: { theme: DEFAULT_THEME, currency },
    createdAt: '2026-01-01T00:00:00Z',
  };
}

describe('CurrencyService', () => {
  let httpMock: HttpTestingController;
  let authService: AuthService;

  function createService(localeId: string = 'en-US'): CurrencyService {
    TestBed.resetTestingModule();
    TestBed.configureTestingModule({
      providers: [
        provideHttpClient(),
        provideHttpClientTesting(),
        { provide: LOCALE_ID, useValue: localeId },
      ],
    });
    httpMock = TestBed.inject(HttpTestingController);
    authService = TestBed.inject(AuthService);

    return TestBed.inject(CurrencyService);
  }

  function login(currency: string = 'EUR'): void {
    authService.login('tester', 'password').subscribe();
    httpMock.expectOne('/api/v1/auth/login').flush(createUser(currency));
  }

  afterEach(() => {
    httpMock.verify();
  });

  it('defaults to USD when there is no authenticated user', () => {
    const currencyService: CurrencyService = createService();

    expect(currencyService.currency()).toBe('USD');
    expect(currencyService.symbol()).toBe('$');
  });

  it('exposes the currency configured in the user settings', () => {
    const currencyService: CurrencyService = createService();
    login('EUR');

    expect(currencyService.currency()).toBe('EUR');
    expect(currencyService.symbol()).toBe('€');
  });

  it('uses the narrow symbol instead of the ISO code in locales such as es-419', () => {
    expect(createService('es-419').symbol()).toBe('$');

    const currencyService: CurrencyService = createService('es-419');
    login('EUR');

    expect(currencyService.symbol()).toBe('€');
  });

  it('falls back to the ISO code for currencies without a symbol', () => {
    const currencyService: CurrencyService = createService();
    login('CHF');

    expect(currencyService.symbol()).toBe('CHF');
  });
});
