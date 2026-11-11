import { formatDate } from '@angular/common';
import { capitalizeFirstLetter } from './string.util';

const DATE_NAME_WIDTHS = ['long', 'short', 'narrow'] as const;
const localeDateNamesCache = new Map<string, ReadonlySet<string>>();

export function formatCapitalizedDate(
  date: Date | string | number,
  format: string,
  locale: string,
): string {
  const formattedDate: string = formatDate(date, format, locale);
  const dateNames: ReadonlySet<string> = getLocaleDateNames(locale);

  return formattedDate.replace(/\p{L}[\p{L}.]*/gu, (word: string) =>
    dateNames.has(word.toLocaleLowerCase(locale)) ? capitalizeFirstLetter(word, locale) : word,
  );
}

function getLocaleDateNames(locale: string): ReadonlySet<string> {
  const cachedNames: ReadonlySet<string> | undefined = localeDateNamesCache.get(locale);
  if (cachedNames != null) {
    return cachedNames;
  }
  const dateNames = new Set<string>();
  for (const width of DATE_NAME_WIDTHS) {
    const monthFormatter = new Intl.DateTimeFormat(locale, {
      month: width,
      timeZone: 'UTC',
    });
    for (let month = 0; month < 12; month++) {
      addDateName(dateNames, monthFormatter.format(new Date(Date.UTC(2026, month, 15))), locale);
    }
    const weekdayFormatter = new Intl.DateTimeFormat(locale, {
      weekday: width,
      timeZone: 'UTC',
    });
    for (let day = 0; day < 7; day++) {
      addDateName(dateNames, weekdayFormatter.format(new Date(Date.UTC(2026, 0, 4 + day))), locale);
    }
  }
  localeDateNamesCache.set(locale, dateNames);
  return dateNames;
}

function addDateName(target: Set<string>, name: string, locale: string): void {
  if (name == null) {
    return;
  }
  target.add(name.toLocaleLowerCase(locale));
}

export function formatYYYYMMDDDate(date: Date | null): string {
  if (date == null) {
    return '';
  }
  return formatDate(date, 'yyyy-MM-dd', 'en-us');
}

// Converts a "yyyy-MM-dd" string to an ISO-8601 instant at the start of that local day,
// e.g. "2024-01-01" -> "2024-01-01T00:00:00.000Z" (adjusted for the local timezone offset).
export function toInstantStart(value: string | null): string | null {
  const date: Date | null = parseYYYYMMDDDate(value);
  if (date == null) {
    return null;
  }
  date.setHours(0, 0, 0, 0);

  return date.toISOString();
}

// Converts a "yyyy-MM-dd" string to an ISO-8601 instant at the end of that local day,
// e.g. "2024-01-31" -> "2024-01-31T23:59:59.999Z" (adjusted for the local timezone offset).
export function toInstantEnd(value: string | null): string | null {
  const date: Date | null = parseYYYYMMDDDate(value);
  if (date == null) {
    return null;
  }
  date.setHours(23, 59, 59, 999);

  return date.toISOString();
}

// Returns every day between both "yyyy-MM-dd" strings, inclusive, as local midnight dates.
// An invalid or reversed range yields an empty list.
export function enumerateDays(start: string | null, end: string | null): Date[] {
  const startDate: Date | null = parseYYYYMMDDDate(start);
  const endDate: Date | null = parseYYYYMMDDDate(end);
  if (startDate == null || endDate == null || startDate.getTime() > endDate.getTime()) {
    return [];
  }
  const days: Date[] = [];
  const current: Date = new Date(startDate);
  while (current.getTime() <= endDate.getTime()) {
    days.push(new Date(current));
    current.setDate(current.getDate() + 1);
  }

  return days;
}

// Returns the first day of every month between both "yyyy-MM-dd" strings, inclusive.
// An invalid or reversed range yields an empty list.
export function enumerateMonths(start: string | null, end: string | null): Date[] {
  const startDate: Date | null = parseYYYYMMDDDate(start);
  const endDate: Date | null = parseYYYYMMDDDate(end);
  if (startDate == null || endDate == null || startDate.getTime() > endDate.getTime()) {
    return [];
  }
  const months: Date[] = [];
  const current: Date = new Date(startDate.getFullYear(), startDate.getMonth(), 1);
  const lastMonth: Date = new Date(endDate.getFullYear(), endDate.getMonth(), 1);
  while (current.getTime() <= lastMonth.getTime()) {
    months.push(new Date(current));
    current.setMonth(current.getMonth() + 1);
  }

  return months;
}

export function parseYYYYMMDDDate(value: string | null): Date | null {
  if (value == null || value === '') {
    return null;
  }
  const parts: string[] = value.split('-');
  if (parts.length !== 3) {
    return null;
  }
  const year: number = Number(parts[0]);
  const month: number = Number(parts[1]);
  const day: number = Number(parts[2]);
  if (!Number.isInteger(year) || !Number.isInteger(month) || !Number.isInteger(day)) {
    return null;
  }
  return new Date(year, month - 1, day);
}
