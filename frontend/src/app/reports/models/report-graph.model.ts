export type ReportGraphType = 'month' | 'year';

export interface ReportGraphAxisConfig {
  stepHours: number;
}

/**
 * Y axis configuration per graph type. The axis max is derived from the tallest bar of the current
 * range (rounded up to the next `stepHours` multiple), so `stepHours` is the only knob here.
 */
export const REPORT_GRAPH_AXIS_CONFIG: Record<ReportGraphType, ReportGraphAxisConfig> = {
  month: { stepHours: 4 },
  year: { stepHours: 40 },
};

const MILLIS_PER_HOUR = 3_600_000;

export interface ReportGraphSegment {
  key: string;
  label: string;
  trackedTimeMillis: number;
  percentage: number;
}

export interface ReportGraphBar {
  key: string;
  primaryLabel: string;
  secondaryLabel: string | null;
  tooltipTitle: string;
  totalTrackedTimeMillis: number;
  segments: ReportGraphSegment[];
}

export interface ReportGraphGroup {
  key: string;
  label: string;
  span: number;
}

export function getAxisStepHours(graphType: ReportGraphType): number {
  return REPORT_GRAPH_AXIS_CONFIG[graphType].stepHours;
}

/**
 * Lowest step multiple that contains every bar. Falls back to a single step when the range holds no
 * tracked time, so the axis never collapses to zero.
 */
export function resolveAxisMaxHours(
  graphType: ReportGraphType,
  maxTrackedTimeMillis: number,
): number {
  const stepHours: number = getAxisStepHours(graphType);
  if (stepHours <= 0) {
    return 0;
  }
  const maxHours: number = maxTrackedTimeMillis / MILLIS_PER_HOUR;
  return Math.max(stepHours, Math.ceil(maxHours / stepHours) * stepHours);
}

export function resolveAxisMaxMillis(
  graphType: ReportGraphType,
  maxTrackedTimeMillis: number,
): number {
  return resolveAxisMaxHours(graphType, maxTrackedTimeMillis) * MILLIS_PER_HOUR;
}

export function buildAxisTicks(graphType: ReportGraphType, maxHours: number): number[] {
  const stepHours: number = getAxisStepHours(graphType);
  if (stepHours <= 0) {
    return [0, maxHours];
  }
  const ticks: number[] = [];
  for (let hour = 0; hour <= maxHours; hour += stepHours) {
    ticks.push(hour);
  }
  if (ticks[ticks.length - 1] !== maxHours) {
    ticks.push(maxHours);
  }
  return ticks;
}
