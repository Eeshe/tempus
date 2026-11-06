import { effect, inject, Service } from '@angular/core';
import type { Theme, ThemeField } from '../user/models/user.model';
import { AuthService } from './auth.service';

export const THEME_STORAGE_KEY = 'tempus.theme';

/**
 * CSS custom properties driven by each theme field. Only these five variables are written at
 * runtime; every other color in styles.css is derived from them with color-mix().
 */
export const THEME_CSS_VARIABLES: Readonly<Record<ThemeField, string>> = {
  background: '--bg',
  surface: '--surface',
  text: '--fg',
  accent: '--good',
  danger: '--bad',
};

/** Mirrors the backend Theme.DEFAULT values so both sides agree on the fallback palette. */
export const DEFAULT_THEME: Readonly<Theme> = {
  background: '#101318',
  surface: '#161a22',
  text: '#e8eaf0',
  accent: '#3dd68c',
  danger: '#f2555c',
};

export const THEME_FIELDS: readonly ThemeField[] = [
  'background',
  'surface',
  'text',
  'accent',
  'danger',
];

const HEX_COLOR_PATTERN = /^#[0-9a-fA-F]{6}$/;

@Service()
export class ThemeService {
  private readonly authService: AuthService = inject(AuthService);

  constructor() {
    effect(() => this.applyCurrentTheme());
  }

  /**
   * Called as an app initializer after the session check, so the first Angular render already uses
   * the palette returned by /me (the inline index.html script covers the paint before that).
   */
  initialize(): void {
    this.applyCurrentTheme();
  }

  applyTheme(theme: Theme): void {
    this.writeThemeVariables(theme);
    this.cacheTheme(theme);
  }

  /**
   * Applies colors without touching the cache: used for the unsaved live preview, so a discarded
   * draft never becomes the palette painted on the next first paint.
   */
  previewTheme(theme: Theme): void {
    this.writeThemeVariables(theme);
  }

  private writeThemeVariables(theme: Theme): void {
    if (typeof document === 'undefined') {
      return;
    }
    for (const field of THEME_FIELDS) {
      document.documentElement.style.setProperty(THEME_CSS_VARIABLES[field], theme[field]);
    }
  }

  private cacheTheme(theme: Theme): void {
    if (typeof localStorage === 'undefined') {
      return;
    }
    try {
      localStorage.setItem(THEME_STORAGE_KEY, JSON.stringify(theme));
    } catch {
      // Storage can be unavailable in private browsing or restricted environments.
    }
  }

  readCachedTheme(): Theme | null {
    if (typeof localStorage === 'undefined') {
      return null;
    }
    try {
      const rawTheme: string | null = localStorage.getItem(THEME_STORAGE_KEY);
      if (rawTheme == null) {
        return null;
      }
      const parsedTheme: unknown = JSON.parse(rawTheme);

      return this.isValidTheme(parsedTheme) ? parsedTheme : null;
    } catch {
      return null;
    }
  }

  clearCachedTheme(): void {
    if (typeof localStorage === 'undefined') {
      return;
    }
    try {
      localStorage.removeItem(THEME_STORAGE_KEY);
    } catch {
      // Storage can be unavailable in private browsing or restricted environments.
    }
  }

  themeEquals(first: Theme, second: Theme): boolean {
    return THEME_FIELDS.every(
      (field: ThemeField) => first[field].toLowerCase() === second[field].toLowerCase(),
    );
  }

  private applyCurrentTheme(): void {
    const theme: Theme | null = this.authService.currentUser()?.settings.theme ?? null;
    if (theme != null) {
      this.applyTheme(theme);
      return;
    }
    // Logged out: fall back to the styles.css defaults and make sure the next visitor of the login
    // page does not get the previous user's palette from the cache.
    this.applyTheme(DEFAULT_THEME);
    this.clearCachedTheme();
  }

  private isValidTheme(value: unknown): value is Theme {
    if (typeof value !== 'object' || value === null) {
      return false;
    }
    const candidate: Record<string, unknown> = value as Record<string, unknown>;

    return THEME_FIELDS.every((field: ThemeField) => this.isValidHexColor(candidate[field]));
  }

  private isValidHexColor(value: unknown): value is string {
    return typeof value === 'string' && HEX_COLOR_PATTERN.test(value);
  }
}
