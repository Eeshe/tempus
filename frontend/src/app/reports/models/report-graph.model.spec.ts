import {
  REPORT_GRAPH_AXIS_CONFIG,
  buildAxisTicks,
  resolveAxisMaxHours,
  resolveAxisMaxMillis,
} from './report-graph.model';

const HOUR_MILLIS = 3_600_000;

describe('report-graph.model', () => {
  describe('axis config', () => {
    it('exposes the tweakable step values', () => {
      expect(REPORT_GRAPH_AXIS_CONFIG.month).toEqual({ stepHours: 4 });
      expect(REPORT_GRAPH_AXIS_CONFIG.year).toEqual({ stepHours: 40 });
    });
  });

  describe('resolveAxisMaxHours', () => {
    it('rounds the tallest month bar up to the next step', () => {
      expect(resolveAxisMaxHours('month', 0)).toBe(4);
      expect(resolveAxisMaxHours('month', 7 * HOUR_MILLIS)).toBe(8);
      expect(resolveAxisMaxHours('month', 4.25 * HOUR_MILLIS)).toBe(8);
      expect(resolveAxisMaxHours('month', 15.5 * HOUR_MILLIS)).toBe(16);
    });

    it('keeps the max when the tallest bar already sits on a step', () => {
      expect(resolveAxisMaxHours('month', 8 * HOUR_MILLIS)).toBe(8);
      expect(resolveAxisMaxHours('month', 20 * HOUR_MILLIS)).toBe(20);
    });

    it('rounds the tallest year bar up to the next step', () => {
      expect(resolveAxisMaxHours('year', 0)).toBe(40);
      expect(resolveAxisMaxHours('year', 40 * HOUR_MILLIS)).toBe(40);
      expect(resolveAxisMaxHours('year', 45 * HOUR_MILLIS)).toBe(80);
      expect(resolveAxisMaxHours('year', 240 * HOUR_MILLIS)).toBe(240);
      expect(resolveAxisMaxHours('year', 721 * HOUR_MILLIS)).toBe(760);
    });
  });

  describe('resolveAxisMaxMillis', () => {
    it('converts the resolved axis max back to millis', () => {
      expect(resolveAxisMaxMillis('month', 7 * HOUR_MILLIS)).toBe(8 * HOUR_MILLIS);
      expect(resolveAxisMaxMillis('year', 0)).toBe(40 * HOUR_MILLIS);
    });
  });

  describe('buildAxisTicks', () => {
    it('builds monthly ticks every 4 hours up to the given max', () => {
      expect(buildAxisTicks('month', 8)).toEqual([0, 4, 8]);
      expect(buildAxisTicks('month', 20)).toEqual([0, 4, 8, 12, 16, 20]);
    });

    it('builds yearly ticks every 40 hours up to the given max', () => {
      expect(buildAxisTicks('year', 120)).toEqual([0, 40, 80, 120]);
      expect(buildAxisTicks('year', 240)).toEqual([0, 40, 80, 120, 160, 200, 240]);
    });

    it('always includes a max that is not a step multiple', () => {
      expect(buildAxisTicks('month', 13)).toEqual([0, 4, 8, 12, 13]);
    });

    it('falls back to a two tick axis when the step is not usable', () => {
      const originalStepHours: number = REPORT_GRAPH_AXIS_CONFIG.month.stepHours;
      REPORT_GRAPH_AXIS_CONFIG.month.stepHours = 0;
      try {
        expect(buildAxisTicks('month', 8)).toEqual([0, 8]);
      } finally {
        REPORT_GRAPH_AXIS_CONFIG.month.stepHours = originalStepHours;
      }
    });
  });
});
