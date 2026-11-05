export type SupportedLocale = 'en-US' | 'es-419';

export interface LocaleOption {
  readonly id: SupportedLocale;
  readonly label: string;
}

export const DEFAULT_LOCALE: SupportedLocale = 'en-US';
export const LOCALE_STORAGE_KEY = 'tempus.locale';

export const SUPPORTED_LOCALES: readonly LocaleOption[] = [
  { id: 'en-US', label: 'English' },
  { id: 'es-419', label: 'Español (Latinoamérica)' },
];

export function isSupportedLocale(value: string | null | undefined): value is SupportedLocale {
  return SUPPORTED_LOCALES.some((locale) => locale.id === value);
}

export function resolveInitialLocale(): SupportedLocale {
  const storedLocale: SupportedLocale | null = readStoredLocale();
  if (storedLocale != null) {
    return storedLocale;
  }
  const browserLocales: readonly string[] = getBrowserLocales();
  for (const browserLocale of browserLocales) {
    const normalizedLocale: SupportedLocale | null = normalizeLocale(browserLocale);
    if (normalizedLocale != null) {
      return normalizedLocale;
    }
  }
  return DEFAULT_LOCALE;
}

function getBrowserLocales(): readonly string[] {
  if (typeof navigator === 'undefined') {
    return [];
  }
  if (navigator.languages?.length > 0) {
    return navigator.languages;
  }
  return navigator.language ? [navigator.language] : [];
}

function readStoredLocale(): SupportedLocale | null {
  if (typeof localStorage === 'undefined') {
    return null;
  }
  try {
    const storedLocale: string | null = localStorage.getItem(LOCALE_STORAGE_KEY);

    return normalizeStoredLocale(storedLocale);
  } catch {
    return null;
  }
}

function normalizeStoredLocale(value: string | null): SupportedLocale | null {
  if (value == null) {
    return null;
  }
  if (isSupportedLocale(value)) {
    return value;
  }
  return normalizeLocale(value);
}

function normalizeLocale(value: string): SupportedLocale | null {
  const normalizedValue: string = value.trim().toLowerCase();
  if (normalizedValue === 'en' || normalizedValue.startsWith('en-')) {
    return 'en-US';
  }
  if (normalizedValue === 'es' || normalizedValue.startsWith('es-')) {
    return 'es-419';
  }
  return null;
}
