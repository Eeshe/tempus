import { getCurrencyOptions } from './currency.util';

describe('currency.util', () => {
  describe('getCurrencyOptions', () => {
    it('returns the currency codes sorted alphabetically', () => {
      const codes: string[] = getCurrencyOptions('en-US').map((option) => option.code);

      expect(codes.length).toBeGreaterThan(0);
      expect(
        [...codes].sort((first: string, second: string) => first.localeCompare(second)),
      ).toEqual(codes);
    });

    it('includes the ISO codes every backend currency round-trips through', () => {
      const codes: string[] = getCurrencyOptions('en-US').map((option) => option.code);

      expect(codes).toContain('USD');
      expect(codes).toContain('EUR');
      expect(codes).toContain('JPY');
    });

    it('labels every currency with a non-empty name', () => {
      const options: { code: string; name: string }[] = getCurrencyOptions('en-US');

      expect(options.every((option) => option.name.trim() !== '')).toBe(true);
    });

    it('names currencies in the requested locale', () => {
      const euros: { code: string; name: string }[] = getCurrencyOptions('es-419').filter(
        (option) => option.code === 'EUR',
      );

      expect(euros).toHaveLength(1);
      expect(euros[0].name.toLowerCase()).not.toBe('eur');
    });

    it('capitalizes the first letter of every name', () => {
      for (const locale of ['en-US', 'es-419']) {
        const uncapitalized: string[] = getCurrencyOptions(locale)
          .map((option) => option.name)
          .filter((name) => name.charAt(0) !== name.charAt(0).toLocaleUpperCase(locale));

        expect(uncapitalized).toEqual([]);
      }
    });

    it('capitalizes lowercase CLDR names such as the Spanish ones', () => {
      const nameOf = (code: string, locale: string): string =>
        getCurrencyOptions(locale).find((option) => option.code === code)!.name;

      expect(nameOf('EUR', 'es-419')).toBe('Euro');
      expect(nameOf('USD', 'es-419')).toBe('Dólar estadounidense');
      expect(nameOf('EUR', 'en-US')).toBe('Euro');
      expect(nameOf('USD', 'en-US')).toBe('US Dollar');
    });
  });
});
