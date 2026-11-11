import { Component, computed, inject, input, LOCALE_ID, output, signal } from '@angular/core';
import { PopupSelectorBase } from '../../../../shared/selector/popup-selector-base';
import {
  formatCapitalizedDate,
  formatYYYYMMDDDate,
  parseYYYYMMDDDate,
} from '../../../../shared/util/date.util';

/** 2024-01-01 is a Monday, so offsets from it label the Monday-first weekday header. */
const WEEKDAY_OFFSETS = [0, 1, 2, 3, 4, 5, 6];
const CALENDAR_MONTH_OFFSETS = [-1, 0];
const DAYS_PER_WEEK: number = 7;
const WEEKS_PER_MONTH: number = 6;
const MILLISECONDS_PER_DAY: number = 86_400_000;

/** Day cell of the two-month calendar. */
export interface CalendarDay {
  readonly formattedDate: string;
  readonly dayOfMonth: number;
  readonly label: string;
  readonly isOutsideMonth: boolean;
  readonly isToday: boolean;
  readonly isSelected: boolean;
  readonly isPreview: boolean;
  readonly isEdge: boolean;
}

/** One of the two months rendered by the calendar. */
export interface CalendarMonth {
  readonly key: string;
  readonly label: string;
  readonly days: CalendarDay[];
}

/** Direction the selected range is moved in by the shift buttons. */
type DateRangeShiftDirection = 'previous' | 'next';

/** Range produced by a shift, ready to be emitted as `datesChangeEvent`. */
interface ShiftedDateRange {
  startDate: string;
  endDate: string;
}

@Component({
  imports: [],
  selector: 'app-date-range-filter',
  styleUrl: './date-range-filter.css',
  templateUrl: './date-range-filter.html',
})
export class DateRangeFilter extends PopupSelectorBase {
  private readonly localeId: string = inject(LOCALE_ID);

  readonly startDate = input<string | null>(null);
  readonly endDate = input<string | null>(null);

  readonly datesChangeEvent = output<{ startDate: string | null, endDate: string | null }>();

  readonly startDateDraft = signal<string | null>(null);

  readonly anchorMonth = signal<Date>(this.getStartOfMonth(new Date()));

  readonly hoveredDate = signal<string | null>(null);

  readonly startLabel: string = $localize`:Date range|Start date placeholder:Start`;
  readonly endLabel: string = $localize`:Date range|End date placeholder:End`;
  readonly previousRangeLabel: string = $localize`:Date range|Previous range button:Previous range`;
  readonly nextRangeLabel: string = $localize`:Date range|Next range button:Next range`;
  readonly previousMonthLabel: string = $localize`:Date range|Previous month button:Previous month`;
  readonly nextMonthLabel: string = $localize`:Date range|Next month button:Next month`;

  readonly weekdayLabels: readonly string[] = WEEKDAY_OFFSETS.map((offset: number): string =>
    formatCapitalizedDate(new Date(2024, 0, 1 + offset), 'EEE', this.localeId),
  );

  readonly canShiftRange = computed<boolean>(() =>
    this.isShiftableDateRange(this.startDate(), this.endDate()),
  );

  private isShiftableDateRange(start: string | null, end: string | null): boolean {
    const startDate: Date | null = parseYYYYMMDDDate(start);
    const endDate: Date | null = parseYYYYMMDDDate(end);

    return startDate != null && endDate != null && startDate.getTime() <= endDate.getTime();
  }

  /**
   * Exactly two consecutive months, so the calendar never grows past that.
  */
  readonly visibleMonths = computed<CalendarMonth[]>(() => {
    const anchor: Date = this.anchorMonth();

    return CALENDAR_MONTH_OFFSETS.map((offset: number): CalendarMonth =>
      this.buildCalendarMonth(this.addMonths(anchor, offset)),
    );
  });

  private buildCalendarMonth(monthStart: Date): CalendarMonth {
    const draft: string | null = this.startDateDraft();
    const hovered: string | null = this.hoveredDate();
    const rangeStart: string | null = draft ?? this.startDate();
    const rangeEnd: string | null = draft == null ? this.endDate() : null;

    // Only a forward span is a valid prospective range; hovering before the draft previews nothing.
    const previewRange: { start: string; end: string } | null = draft != null
      && hovered != null && hovered >= draft ? { start: draft, end: hovered } : null;
    const today: string = formatYYYYMMDDDate(new Date());
    const mondayOffset: number = (monthStart.getDay() + 6) % 7;
    const firstCell: Date = new Date(
      monthStart.getFullYear(),
      monthStart.getMonth(),
      1 - mondayOffset,
    );
    const days: CalendarDay[] = [];
    for (let index = 0; index < DAYS_PER_WEEK * WEEKS_PER_MONTH; index++) {
      const cellDate: Date = new Date(
        firstCell.getFullYear(),
        firstCell.getMonth(),
        firstCell.getDate() + index,
      );
      const formattedDate: string = formatYYYYMMDDDate(cellDate);
      const isOutsideMonth: boolean = cellDate.getMonth() !== monthStart.getMonth();
      const isInRange: boolean =
        !isOutsideMonth &&
        rangeStart != null && rangeEnd != null &&
        formattedDate >= rangeStart && formattedDate <= rangeEnd;
      const isDraftDay: boolean = !isOutsideMonth && draft != null && formattedDate === draft;
      const isSelected: boolean = isInRange || isDraftDay;
      const isPreview: boolean =
        !isOutsideMonth &&
        !isDraftDay &&
        previewRange != null &&
        formattedDate >= previewRange.start &&
        formattedDate <= previewRange.end;
      const isEdge: boolean =
        isSelected &&
        (formattedDate === rangeStart || formattedDate === rangeEnd || isDraftDay || rangeStart === rangeEnd);

      days.push({
        formattedDate,
        dayOfMonth: cellDate.getDate(),
        label: formatCapitalizedDate(cellDate, 'MMMM d, y', this.localeId),
        isOutsideMonth,
        isToday: !isOutsideMonth && formattedDate === today,
        isSelected,
        isPreview,
        isEdge,
      });
    }

    return {
      key: formatYYYYMMDDDate(monthStart),
      label: formatCapitalizedDate(monthStart, 'MMM y', this.localeId),
      days,
    };
  }

  override toggle(): void {
    if (!this.isOpen()) {
      this.startDateDraft.set(null);
      this.hoveredDate.set(null);
      this.anchorVisibleMonth(this.endDate());
    }
    super.toggle();
  }

  protected override closeSelector(): void {
    this.startDateDraft.set(null);
    this.hoveredDate.set(null);
    super.closeSelector();
  }

  selectDay(date: string, outsideMonth: boolean = false): void {
    // A dimmed cell belongs to the neighboring month: anchor on that month so the picked day (and
    // the resulting range) is rendered in its own grid instead of the dimmed duplicate.
    if (outsideMonth) {
      this.anchorVisibleMonth(date);
    }
    const draft: string | null = this.startDateDraft();
    if (draft == null || date < draft) {
      this.startDateDraft.set(date);
      return;
    }
    this.startDateDraft.set(null);
    this.hoveredDate.set(null);
    this.datesChangeEvent.emit({ startDate: draft, endDate: date });
  }

  hoverDay(date: string): void {
    this.hoveredDate.set(date);
  }

  clearHoveredDay(): void {
    this.hoveredDate.set(null);
  }

  shiftVisibleMonths(offset: number): void {
    this.anchorMonth.set(this.addMonths(this.anchorMonth(), offset));
  }

  private addMonths(date: Date, months: number): Date {
    return new Date(date.getFullYear(), date.getMonth() + months, 1);
  }

  shiftRange(direction: DateRangeShiftDirection): void {
    const shifted: ShiftedDateRange | null = this.shiftDateRange(
      this.startDate(),
      this.endDate(),
      direction,
    );
    if (shifted == null) {
      return;
    }
    this.startDateDraft.set(null);
    this.hoveredDate.set(null);
    this.anchorVisibleMonth(shifted.endDate);
    this.datesChangeEvent.emit(shifted);
  }

  // Moves a range one unit forward or backward: a full month to the neighboring month, a full year
  // to the neighboring year, and anything else by the number of days it currently spans.
  // Incomplete, malformed or reversed ranges cannot be shifted and yield null.
  private shiftDateRange(
    start: string | null,
    end: string | null,
    direction: DateRangeShiftDirection,
  ): ShiftedDateRange | null {
    const startDate: Date | null = parseYYYYMMDDDate(start);
    const endDate: Date | null = parseYYYYMMDDDate(end);
    if (startDate == null || endDate == null || startDate.getTime() > endDate.getTime()) {
      return null;
    }
    const step: number = direction === 'next' ? 1 : -1;

    if (this.isFullYear(startDate, endDate)) {
      const year: number = startDate.getFullYear() + step;

      return this.toShiftedRange(new Date(year, 0, 1), new Date(year, 11, 31));
    }
    if (this.isFullMonth(startDate, endDate)) {
      const year: number = startDate.getFullYear();
      const month: number = startDate.getMonth() + step;

      return this.toShiftedRange(new Date(year, month, 1), new Date(year, month + 1, 0));
    }
    const spanDays: number = this.countDaysInclusive(startDate, endDate) * step;

    return this.toShiftedRange(
      new Date(startDate.getFullYear(), startDate.getMonth(), startDate.getDate() + spanDays),
      new Date(endDate.getFullYear(), endDate.getMonth(), endDate.getDate() + spanDays),
    );
  }

  private isFullYear(start: Date, end: Date): boolean {
    return (
      start.getFullYear() === end.getFullYear() &&
      start.getMonth() === 0 &&
      start.getDate() === 1 &&
      end.getMonth() === 11 &&
      end.getDate() === 31
    );
  }

  private isFullMonth(start: Date, end: Date): boolean {
    return (
      start.getFullYear() === end.getFullYear() &&
      start.getMonth() === end.getMonth() &&
      start.getDate() === 1 &&
      end.getDate() === new Date(end.getFullYear(), end.getMonth() + 1, 0).getDate()
    );
  }

  private countDaysInclusive(start: Date, end: Date): number {
    const startUtc: number = Date.UTC(start.getFullYear(), start.getMonth(), start.getDate());
    const endUtc: number = Date.UTC(end.getFullYear(), end.getMonth(), end.getDate());

    return Math.round((endUtc - startUtc) / MILLISECONDS_PER_DAY) + 1;
  }

  private toShiftedRange(start: Date, end: Date): ShiftedDateRange {
    return { startDate: formatYYYYMMDDDate(start), endDate: formatYYYYMMDDDate(end) };
  }

  private anchorVisibleMonth(date: string | null): void {
    const anchor: Date | null = parseYYYYMMDDDate(date);

    this.anchorMonth.set(this.getStartOfMonth(anchor ?? new Date()));
  }

  private getStartOfMonth(date: Date): Date {
    return new Date(date.getFullYear(), date.getMonth(), 1);
  }

  setPreset(preset: string): void {
    const today: Date = new Date();
    const year: number = today.getFullYear();
    const month: number = today.getMonth();
    const day: number = today.getDate();
    const dayOfWeek: number = today.getDay();
    const mondayOffset: number = dayOfWeek === 0 ? 6 : dayOfWeek - 1;

    let start: Date;
    let end: Date;

    switch (preset) {
      case 'today':
        start = new Date(year, month, day);
        end = new Date(year, month, day);
        break;
      case 'yesterday':
        start = new Date(year, month, day - 1);
        end = new Date(year, month, day - 1);
        break;
      case 'thisWeek':
        start = new Date(year, month, day - mondayOffset);
        end = new Date(year, month, day - mondayOffset + 6);
        break;
      case 'lastWeek':
        start = new Date(year, month, day - mondayOffset - 7);
        end = new Date(year, month, day - mondayOffset - 1);
        break;
      case 'thisMonth':
        start = new Date(year, month, 1);
        end = new Date(year, month + 1, 0);
        break;
      case 'lastMonth':
        start = new Date(year, month - 1, 1);
        end = new Date(year, month, 0);
        break;
      case 'thisYear':
        start = new Date(year, 0, 1);
        end = new Date(year, 11, 31);
        break;
      case 'lastYear':
        start = new Date(year - 1, 0, 1);
        end = new Date(year - 1, 11, 31);
        break;
      default:
        return;
    }
    start.setHours(0);
    end.setHours(23);

    const startStr: string = formatYYYYMMDDDate(start);
    const endStr: string = formatYYYYMMDDDate(end);

    this.startDateDraft.set(null);
    this.hoveredDate.set(null);
    this.anchorVisibleMonth(endStr);
    this.datesChangeEvent.emit({ startDate: startStr, endDate: endStr });
  }
}
