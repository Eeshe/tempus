/** Uppercases only the first character, following the locale's casing rules ("euro" → "Euro"). */
export function capitalizeFirstLetter(value: string, locale: string): string {
  if (value === '') {
    return value;
  }

  return value.charAt(0).toLocaleUpperCase(locale) + value.slice(1);
}

/**
 * Lowercases and strips the diacritics of a value, so a search for "dolar" also matches "Dólar".
 * Both the query and the searched text have to go through it for the comparison to be meaningful.
 */
export function normalizeForSearch(value: string): string {
  return value
    .normalize('NFD')
    .replace(/\p{Diacritic}/gu, '')
    .toLowerCase()
    .trim();
}
