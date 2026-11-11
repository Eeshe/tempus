import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { DEFAULT_CURRENCY } from '../shared/util/currency.util';
import type { Theme, User } from '../user/models/user.model';
import { AuthService } from './auth.service';
import { DEFAULT_THEME, THEME_STORAGE_KEY, ThemeService } from './theme.service';

const CUSTOM_THEME: Theme = {
  background: '#0a0b0d',
  surface: '#111318',
  text: '#f2f4f8',
  accent: '#00d1b2',
  danger: '#ff4d4f',
};

const CSS_VARIABLES: readonly string[] = ['--bg', '--surface', '--fg', '--good', '--bad'];

function createUser(theme: Theme): User {
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
  for (const name of CSS_VARIABLES) {
    document.documentElement.style.removeProperty(name);
  }
}

describe('ThemeService', () => {
  let httpMock: HttpTestingController;
  let authService: AuthService;
  let themeService: ThemeService;

  beforeEach(() => {
    localStorage.clear();
    clearVariables();
    TestBed.configureTestingModule({
      providers: [provideHttpClient(), provideHttpClientTesting()],
    });
    httpMock = TestBed.inject(HttpTestingController);
    authService = TestBed.inject(AuthService);
    themeService = TestBed.inject(ThemeService);
  });

  afterEach(() => {
    httpMock.verify();
    localStorage.clear();
    clearVariables();
  });

  function authenticate(user: User): void {
    authService.checkAuthentication().subscribe();
    httpMock.expectOne('/api/v1/auth/me').flush(user);
  }

  it('applies the user palette to the document root and caches it', () => {
    authenticate(createUser(CUSTOM_THEME));

    themeService.initialize();

    expect(readVariable('--bg')).toBe(CUSTOM_THEME.background);
    expect(readVariable('--surface')).toBe(CUSTOM_THEME.surface);
    expect(readVariable('--fg')).toBe(CUSTOM_THEME.text);
    expect(readVariable('--good')).toBe(CUSTOM_THEME.accent);
    expect(readVariable('--bad')).toBe(CUSTOM_THEME.danger);
    expect(JSON.parse(localStorage.getItem(THEME_STORAGE_KEY)!)).toEqual(CUSTOM_THEME);
  });

  it('falls back to the default palette and clears the cache when logged out', () => {
    localStorage.setItem(THEME_STORAGE_KEY, JSON.stringify(CUSTOM_THEME));

    themeService.initialize();

    expect(readVariable('--bg')).toBe(DEFAULT_THEME.background);
    expect(readVariable('--good')).toBe(DEFAULT_THEME.accent);
    expect(localStorage.getItem(THEME_STORAGE_KEY)).toBeNull();
  });

  it('previews colors without caching them', () => {
    themeService.previewTheme(CUSTOM_THEME);

    expect(readVariable('--bg')).toBe(CUSTOM_THEME.background);
    expect(readVariable('--bad')).toBe(CUSTOM_THEME.danger);
    expect(localStorage.getItem(THEME_STORAGE_KEY)).toBeNull();
  });

  it('matches the backend default palette', () => {
    expect(DEFAULT_THEME).toEqual({
      background: '#101318',
      surface: '#161a22',
      text: '#e8eaf0',
      accent: '#3dd68c',
      danger: '#f2555c',
    });
  });

  it('compares themes ignoring color case', () => {
    expect(themeService.themeEquals(DEFAULT_THEME, { ...DEFAULT_THEME })).toBe(true);
    expect(
      themeService.themeEquals(DEFAULT_THEME, {
        ...DEFAULT_THEME,
        background: DEFAULT_THEME.background.toUpperCase(),
      }),
    ).toBe(true);
    expect(themeService.themeEquals(DEFAULT_THEME, { ...DEFAULT_THEME, danger: '#000000' })).toBe(
      false,
    );
  });

  it('reads a valid cached theme', () => {
    localStorage.setItem(THEME_STORAGE_KEY, JSON.stringify(CUSTOM_THEME));

    expect(themeService.readCachedTheme()).toEqual(CUSTOM_THEME);
  });

  it('ignores a cached theme with a non-hex color', () => {
    localStorage.setItem(THEME_STORAGE_KEY, JSON.stringify({ ...CUSTOM_THEME, background: 'red' }));

    expect(themeService.readCachedTheme()).toBeNull();
  });

  it('ignores a cached theme with a short hex color', () => {
    localStorage.setItem(THEME_STORAGE_KEY, JSON.stringify({ ...CUSTOM_THEME, accent: '#a1b2c' }));

    expect(themeService.readCachedTheme()).toBeNull();
  });

  it('ignores a cached theme missing fields', () => {
    localStorage.setItem(THEME_STORAGE_KEY, JSON.stringify({ background: '#000000' }));

    expect(themeService.readCachedTheme()).toBeNull();
  });

  it('ignores a cached theme that is not an object', () => {
    localStorage.setItem(THEME_STORAGE_KEY, JSON.stringify('nope'));
    expect(themeService.readCachedTheme()).toBeNull();

    localStorage.setItem(THEME_STORAGE_KEY, JSON.stringify(null));
    expect(themeService.readCachedTheme()).toBeNull();
  });

  it('ignores a cached theme that is not valid JSON', () => {
    localStorage.setItem(THEME_STORAGE_KEY, '{not json');

    expect(themeService.readCachedTheme()).toBeNull();
  });
});
