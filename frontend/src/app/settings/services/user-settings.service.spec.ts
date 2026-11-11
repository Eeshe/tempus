import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { DEFAULT_THEME } from '../../services/theme.service';
import type { Theme, UserSettings } from '../../user/models/user.model';
import { UserSettingsService } from './user-settings.service';

describe('UserSettingsService', () => {
  let httpMock: HttpTestingController;
  let userSettingsService: UserSettingsService;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [provideHttpClient(), provideHttpClientTesting()],
    });
    httpMock = TestBed.inject(HttpTestingController);
    userSettingsService = TestBed.inject(UserSettingsService);
  });

  afterEach(() => {
    httpMock.verify();
  });

  it('patches the full theme', () => {
    const theme: Theme = { ...DEFAULT_THEME, danger: '#aa0000' };
    let response: UserSettings | undefined;

    userSettingsService
      .patchTheme(theme)
      .subscribe((settings: UserSettings) => (response = settings));

    const request = httpMock.expectOne('/api/v1/settings');
    expect(request.request.method).toBe('PATCH');
    expect(request.request.body).toEqual({ theme });
    expect(request.request.withCredentials).toBe(true);

    request.flush({ theme, currency: 'USD' });

    expect(response).toEqual({ theme, currency: 'USD' });
  });

  it('patches the currency on its own', () => {
    let response: UserSettings | undefined;

    userSettingsService
      .patchCurrency('EUR')
      .subscribe((settings: UserSettings) => (response = settings));

    const request = httpMock.expectOne('/api/v1/settings');
    expect(request.request.method).toBe('PATCH');
    expect(request.request.body).toEqual({ currency: 'EUR' });
    expect(request.request.withCredentials).toBe(true);

    const settings: UserSettings = { theme: DEFAULT_THEME, currency: 'EUR' };
    request.flush(settings);

    expect(response).toEqual(settings);
  });
});
