import { capitalizeFirstLetter } from './string.util';

export const DEFAULT_CURRENCY = 'USD';

export interface CurrencyOption {
  readonly code: string;
  readonly name: string;
}

/**
 * Used when Intl.supportedValuesOf is unavailable
 */
const FALLBACK_CURRENCY_CODES: readonly string[] = [
  'AUD',
  'BRL',
  'CAD',
  'CHF',
  'CNY',
  'EUR',
  'GBP',
  'INR',
  'JPY',
  'KRW',
  'MXN',
  'NOK',
  'NZD',
  'SEK',
  'USD',
  'UYU',
  'ZAR',
];

export function getCurrencyOptions(locale: string): CurrencyOption[] {
  const displayNames: Intl.DisplayNames | null = createDisplayNames(locale);

  return getCurrencyCodes().map((code: string) => ({
    code: code,
    name: resolveCurrencyName(code, displayNames, locale),
  }));
}

function getCurrencyCodes(): string[] {
  if (typeof Intl.supportedValuesOf !== 'function') {
    return [...FALLBACK_CURRENCY_CODES];
  }
  try {
    const codes: string[] = Intl.supportedValuesOf('currency');
    if (codes.length === 0) {
      return [...FALLBACK_CURRENCY_CODES];
    }
    return [...codes].sort((first: string, second: string) => first.localeCompare(second));
  } catch {
    return [...FALLBACK_CURRENCY_CODES];
  }
}

function createDisplayNames(locale: string): Intl.DisplayNames | null {
  if (typeof Intl.DisplayNames !== 'function') {
    return null;
  }
  try {
    return new Intl.DisplayNames([locale], { type: 'currency' });
  } catch {
    return null;
  }
}

function resolveCurrencyName(
  code: string,
  displayNames: Intl.DisplayNames | null,
  locale: string,
): string {
  if (displayNames == null) {
    return code;
  }
  try {
    // CLDR keeps currency names lowercase in locales such as es-419 ("euro"), so the first letter
    // is uppercased with the locale's own casing rules for a readable dropdown.
    return capitalizeFirstLetter(displayNames.of(code) ?? code, locale);
  } catch {
    return code;
  }
}
