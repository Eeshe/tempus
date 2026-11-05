import { vi } from 'vitest';
import {
  DEFAULT_LOCALE,
  LOCALE_STORAGE_KEY,
  resolveInitialLocale,
} from './locale.constants';

describe('resolveInitialLocale', () => {
  afterEach(() => {
    localStorage.clear();
    vi.restoreAllMocks();
  });

  it('uses a supported locale from localStorage', () => {
    localStorage.setItem(LOCALE_STORAGE_KEY, 'es-419');

    expect(resolveInitialLocale()).toBe('es-419');
  });

  it('normalizes a stored language-only locale', () => {
    localStorage.setItem(LOCALE_STORAGE_KEY, 'es');

    expect(resolveInitialLocale()).toBe('es-419');
  });

  it('falls back to the browser language', () => {
    vi.spyOn(navigator, 'languages', 'get').mockReturnValue(['es-MX', 'en-US']);

    expect(resolveInitialLocale()).toBe('es-419');
  });

  it('falls back to English when nothing matches', () => {
    vi.spyOn(navigator, 'languages', 'get').mockReturnValue(['fr-FR']);

    expect(resolveInitialLocale()).toBe(DEFAULT_LOCALE);
  });

  it('ignores an unsupported stored locale', () => {
    localStorage.setItem(LOCALE_STORAGE_KEY, 'fr-FR');
    vi.spyOn(navigator, 'languages', 'get').mockReturnValue(['en-GB']);

    expect(resolveInitialLocale()).toBe('en-US');
  });
});
