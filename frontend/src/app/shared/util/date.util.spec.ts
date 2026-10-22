import { enumerateDays, enumerateMonths, formatYYYYMMDDDate, parseYYYYMMDDDate } from './date.util';

describe('date.util', () => {
  describe('parseYYYYMMDDDate', () => {
    it('parses a yyyy-MM-dd string into a local midnight date', () => {
      expect(parseYYYYMMDDDate('2026-08-21')).toEqual(new Date(2026, 7, 21));
    });

    it('returns null for null, empty and malformed values', () => {
      expect(parseYYYYMMDDDate(null)).toBeNull();
      expect(parseYYYYMMDDDate('')).toBeNull();
      expect(parseYYYYMMDDDate('2026/08/21')).toBeNull();
      expect(parseYYYYMMDDDate('2026-08')).toBeNull();
    });
  });

  describe('enumerateDays', () => {
    it('returns a single day when start and end match', () => {
      expect(enumerateDays('2026-08-21', '2026-08-21')).toEqual([new Date(2026, 7, 21)]);
    });

    it('returns every day of the range, inclusive', () => {
      const days: Date[] = enumerateDays('2026-08-30', '2026-09-02');

      expect(days.map(formatYYYYMMDDDate)).toEqual([
        '2026-08-30',
        '2026-08-31',
        '2026-09-01',
        '2026-09-02',
      ]);
    });

    it('returns an empty list for a reversed or invalid range', () => {
      expect(enumerateDays('2026-08-22', '2026-08-21')).toEqual([]);
      expect(enumerateDays(null, '2026-08-21')).toEqual([]);
      expect(enumerateDays('not-a-date', '2026-08-21')).toEqual([]);
    });
  });

  describe('enumerateMonths', () => {
    it('returns the first day of every month in the range', () => {
      const months: Date[] = enumerateMonths('2026-07-15', '2026-09-02');

      expect(months.map(formatYYYYMMDDDate)).toEqual(['2026-07-01', '2026-08-01', '2026-09-01']);
    });

    it('spans year boundaries', () => {
      const months: Date[] = enumerateMonths('2025-11-10', '2026-02-05');

      expect(months.map(formatYYYYMMDDDate)).toEqual([
        '2025-11-01',
        '2025-12-01',
        '2026-01-01',
        '2026-02-01',
      ]);
    });

    it('returns an empty list for a reversed or invalid range', () => {
      expect(enumerateMonths('2026-09-01', '2026-08-01')).toEqual([]);
      expect(enumerateMonths('2026-08-01', null)).toEqual([]);
    });
  });
});
