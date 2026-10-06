import { formatDate } from '@angular/common';

export function formatYYYYMMDDDate(date: Date | null): string {
  if (date == null) {
    return "";
  }
  return formatDate(date, "yyyy-MM-dd", "en-us");
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

function parseYYYYMMDDDate(value: string | null): Date | null {
  if (value == null || value === "") {
    return null;
  }
  const parts: string[] = value.split("-");
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
