import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import type { Theme, User } from '../user/models/user.model';
import { AuthService } from './auth.service';
import { DEFAULT_THEME } from './theme.service';

function createUser(theme: Theme = DEFAULT_THEME): User {
  return {
    id: 1,
    name: 'tester',
    settings: { theme },
    createdAt: '2026-01-01T00:00:00Z',
  };
}

describe('AuthService', () => {
  let httpMock: HttpTestingController;
  let authService: AuthService;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [provideHttpClient(), provideHttpClientTesting()],
    });
    httpMock = TestBed.inject(HttpTestingController);
    authService = TestBed.inject(AuthService);
  });

  afterEach(() => {
    httpMock.verify();
  });

  it('stores the user returned by the session check', () => {
    let isAuthenticated: boolean | undefined;
    authService.checkAuthentication().subscribe((result: boolean) => (isAuthenticated = result));

    httpMock.expectOne('/api/v1/auth/me').flush(createUser());

    expect(isAuthenticated).toBe(true);
    expect(authService.isAuthenticated()).toBe(true);
    expect(authService.currentUser()).toEqual(createUser());
  });

  it('clears the user when the session check fails', () => {
    let isAuthenticated: boolean | undefined;
    authService.checkAuthentication().subscribe((result: boolean) => (isAuthenticated = result));

    httpMock.expectOne('/api/v1/auth/me').flush({}, { status: 401, statusText: 'Unauthorized' });

    expect(isAuthenticated).toBe(false);
    expect(authService.isAuthenticated()).toBe(false);
    expect(authService.currentUser()).toBeNull();
  });

  it('stores the user returned by login', () => {
    authService.login('tester', 'password').subscribe();

    httpMock.expectOne('/api/v1/auth/login').flush(createUser());

    expect(authService.isAuthenticated()).toBe(true);
    expect(authService.currentUser()).toEqual(createUser());
  });

  it('stores the user returned by register', () => {
    authService.register('tester', 'password').subscribe();

    httpMock.expectOne('/api/v1/auth/register').flush(createUser());

    expect(authService.currentUser()).toEqual(createUser());
  });

  it('clears the user on logout', () => {
    authService.login('tester', 'password').subscribe();
    httpMock.expectOne('/api/v1/auth/login').flush(createUser());

    authService.logout().subscribe();
    httpMock.expectOne('/api/v1/auth/logout').flush(null);

    expect(authService.isAuthenticated()).toBe(false);
    expect(authService.currentUser()).toBeNull();
  });

  it('updates the locally stored theme', () => {
    authService.login('tester', 'password').subscribe();
    httpMock.expectOne('/api/v1/auth/login').flush(createUser());

    const updatedTheme: Theme = { ...DEFAULT_THEME, accent: '#123456' };
    authService.setTheme(updatedTheme);

    expect(authService.currentUser()?.settings.theme).toEqual(updatedTheme);
  });
});
