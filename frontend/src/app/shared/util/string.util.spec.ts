import { capitalizeFirstLetter, normalizeForSearch } from './string.util';

describe('string.util', () => {
  describe('capitalizeFirstLetter', () => {
    it('uppercases only the first character', () => {
      expect(capitalizeFirstLetter('euro', 'en-US')).toBe('Euro');
      expect(capitalizeFirstLetter('dólar estadounidense', 'es-419')).toBe('Dólar estadounidense');
    });

    it('leaves an already capitalized value untouched', () => {
      expect(capitalizeFirstLetter('US Dollar', 'en-US')).toBe('US Dollar');
    });

    it('handles empty values', () => {
      expect(capitalizeFirstLetter('', 'en-US')).toBe('');
    });
  });

  describe('normalizeForSearch', () => {
    it('lowercases the value', () => {
      expect(normalizeForSearch('EURO')).toBe('euro');
    });

    it('strips the diacritics', () => {
      expect(normalizeForSearch('Dólar estadounidense')).toBe('dolar estadounidense');
      expect(normalizeForSearch('Yen japonés')).toBe('yen japones');
    });

    it('trims the surrounding whitespace', () => {
      expect(normalizeForSearch('  Euro  ')).toBe('euro');
    });
  });
});
