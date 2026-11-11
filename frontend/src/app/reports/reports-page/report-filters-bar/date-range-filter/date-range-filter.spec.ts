import { ComponentFixture, TestBed } from '@angular/core/testing';
import { DateRangeFilter } from './date-range-filter';
import type { CalendarDay, CalendarMonth } from './date-range-filter';

interface ShiftCase {
  description: string;
  start: string;
  end: string;
  direction: 'previous' | 'next';
  expected: { startDate: string; endDate: string };
}

describe('DateRangeFilter', () => {
  let component: DateRangeFilter;
  let fixture: ComponentFixture<DateRangeFilter>;

  const shiftCases: ShiftCase[] = [
    {
      description: 'shifts a full month to the next month',
      start: '2024-11-01',
      end: '2024-11-30',
      direction: 'next',
      expected: { startDate: '2024-12-01', endDate: '2024-12-31' },
    },
    {
      description: 'shifts a full month to the previous month',
      start: '2024-11-01',
      end: '2024-11-30',
      direction: 'previous',
      expected: { startDate: '2024-10-01', endDate: '2024-10-31' },
    },
    {
      description: 'shifts a full month across a year boundary',
      start: '2024-01-01',
      end: '2024-01-31',
      direction: 'previous',
      expected: { startDate: '2023-12-01', endDate: '2023-12-31' },
    },
    {
      description: 'shifts December to January of the next year',
      start: '2024-12-01',
      end: '2024-12-31',
      direction: 'next',
      expected: { startDate: '2025-01-01', endDate: '2025-01-31' },
    },
    {
      description: 'uses the leap-year length of February',
      start: '2024-03-01',
      end: '2024-03-31',
      direction: 'previous',
      expected: { startDate: '2024-02-01', endDate: '2024-02-29' },
    },
    {
      description: 'uses the non-leap-year length of February',
      start: '2023-03-01',
      end: '2023-03-31',
      direction: 'previous',
      expected: { startDate: '2023-02-01', endDate: '2023-02-28' },
    },
    {
      description: 'shifts a full year to the next year',
      start: '2024-01-01',
      end: '2024-12-31',
      direction: 'next',
      expected: { startDate: '2025-01-01', endDate: '2025-12-31' },
    },
    {
      description: 'shifts a full year to the previous year',
      start: '2024-01-01',
      end: '2024-12-31',
      direction: 'previous',
      expected: { startDate: '2023-01-01', endDate: '2023-12-31' },
    },
    {
      description: 'moves any other range forward by the number of days it spans',
      start: '2024-11-02',
      end: '2024-11-06',
      direction: 'next',
      expected: { startDate: '2024-11-07', endDate: '2024-11-11' },
    },
    {
      description: 'moves any other range backward by the number of days it spans',
      start: '2024-11-02',
      end: '2024-11-06',
      direction: 'previous',
      expected: { startDate: '2024-10-28', endDate: '2024-11-01' },
    },
    {
      description: 'shifts a single day',
      start: '2024-11-02',
      end: '2024-11-02',
      direction: 'next',
      expected: { startDate: '2024-11-03', endDate: '2024-11-03' },
    },
    {
      description: 'shifts a day range across a month boundary',
      start: '2024-01-28',
      end: '2024-01-31',
      direction: 'next',
      expected: { startDate: '2024-02-01', endDate: '2024-02-04' },
    },
    {
      description: 'does not treat a partial month as a full month',
      start: '2024-11-01',
      end: '2024-11-29',
      direction: 'next',
      expected: { startDate: '2024-11-30', endDate: '2024-12-28' },
    },
  ];

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [DateRangeFilter],
    }).compileComponents();

    fixture = TestBed.createComponent(DateRangeFilter);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  function setRange(startDate: string | null, endDate: string | null): void {
    fixture.componentRef.setInput('startDate', startDate);
    fixture.componentRef.setInput('endDate', endDate);
  }

  function collectEmissions(): { startDate: string | null; endDate: string | null }[] {
    const emitted: { startDate: string | null; endDate: string | null }[] = [];
    component.datesChangeEvent.subscribe((value) => emitted.push(value));

    return emitted;
  }

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('renders the shift arrows inside the date range filter wrapper', () => {
    fixture.detectChanges();
    const selector: HTMLElement = fixture.nativeElement.querySelector('.selector');

    expect(selector.querySelectorAll('.date-range-filter__shift-button').length).toBe(2);
    expect(selector.querySelectorAll('.date-range-filter__divider').length).toBe(1);
  });

  it('toggles the popup when the pill is clicked', () => {
    fixture.detectChanges();
    const pill: HTMLElement = fixture.nativeElement.querySelector('.selector__trigger');

    pill.click();
    expect(component.isOpen()).toBe(true);

    pill.click();
    expect(component.isOpen()).toBe(false);
  });

  it('toggles the popup once when the label is clicked', () => {
    fixture.detectChanges();
    const label: HTMLElement = fixture.nativeElement.querySelector('.date-range-filter__label');

    label.click();
    expect(component.isOpen()).toBe(true);

    label.click();
    expect(component.isOpen()).toBe(false);
  });

  it('does not toggle the popup when a shift arrow is clicked', () => {
    setRange('2024-11-02', '2024-11-06');
    fixture.detectChanges();
    const pill: HTMLElement = fixture.nativeElement.querySelector('.selector__trigger');
    const arrow: HTMLButtonElement = fixture.nativeElement.querySelector(
      '.date-range-filter__shift-button--next',
    );

    arrow.click();
    expect(component.isOpen()).toBe(false);

    pill.click();
    expect(component.isOpen()).toBe(true);

    arrow.click();
    expect(component.isOpen()).toBe(true);
  });

  it('allows shifting only a complete, forward range', () => {
    setRange(null, null);
    expect(component.canShiftRange()).toBe(false);

    setRange('2024-11-02', null);
    expect(component.canShiftRange()).toBe(false);

    setRange('2024-11-07', '2024-11-02');
    expect(component.canShiftRange()).toBe(false);

    setRange('2024-11-02', '2024-11-06');
    expect(component.canShiftRange()).toBe(true);

    setRange('2024-11-02', '2024-11-02');
    expect(component.canShiftRange()).toBe(true);
  });

  for (const testCase of shiftCases) {
    it(testCase.description, () => {
      setRange(testCase.start, testCase.end);
      const emitted: { startDate: string | null; endDate: string | null }[] = collectEmissions();

      component.shiftRange(testCase.direction);

      expect(emitted).toEqual([testCase.expected]);
    });
  }

  it('does not emit for an incomplete or malformed range', () => {
    setRange('2024-11-02', null);
    const emitted: { startDate: string | null; endDate: string | null }[] = collectEmissions();

    component.shiftRange('next');
    expect(emitted).toEqual([]);

    setRange('2024-11', '2024-11-06');
    component.shiftRange('next');
    expect(emitted).toEqual([]);
  });

  it('does not emit for a reversed range', () => {
    setRange('2024-11-07', '2024-11-02');
    const emitted: { startDate: string | null; endDate: string | null }[] = collectEmissions();

    component.shiftRange('previous');

    expect(emitted).toEqual([]);
  });

  describe('presets', () => {
    // Presets read the real clock, so each call runs with a frozen system time.
    function presetRange(
      preset: string,
      today: Date,
    ): { startDate: string | null; endDate: string | null }[] {
      vi.useFakeTimers();
      vi.setSystemTime(today);
      try {
        const emitted: { startDate: string | null; endDate: string | null }[] = collectEmissions();
        component.setPreset(preset);

        return emitted;
      } finally {
        vi.useRealTimers();
      }
    }

    it('selects the whole current month', () => {
      expect(presetRange('thisMonth', new Date(2024, 10, 9, 12))).toEqual([
        { startDate: '2024-11-01', endDate: '2024-11-30' },
      ]);
    });

    it('uses the leap-year length of the current month', () => {
      expect(presetRange('thisMonth', new Date(2024, 1, 15, 12))).toEqual([
        { startDate: '2024-02-01', endDate: '2024-02-29' },
      ]);
    });

    it('uses the non-leap-year length of the current month', () => {
      expect(presetRange('thisMonth', new Date(2023, 1, 15, 12))).toEqual([
        { startDate: '2023-02-01', endDate: '2023-02-28' },
      ]);
    });

    it('selects Monday to Sunday of the current week', () => {
      expect(presetRange('thisWeek', new Date(2024, 10, 6, 12))).toEqual([
        { startDate: '2024-11-04', endDate: '2024-11-10' },
      ]);
    });

    it('keeps Sunday in the week that started the previous Monday', () => {
      expect(presetRange('thisWeek', new Date(2024, 10, 10, 12))).toEqual([
        { startDate: '2024-11-04', endDate: '2024-11-10' },
      ]);
    });

    it('selects a week spanning a year boundary', () => {
      expect(presetRange('thisWeek', new Date(2024, 11, 31, 12))).toEqual([
        { startDate: '2024-12-30', endDate: '2025-01-05' },
      ]);
    });

    it('selects the whole current year', () => {
      expect(presetRange('thisYear', new Date(2024, 10, 9, 12))).toEqual([
        { startDate: '2024-01-01', endDate: '2024-12-31' },
      ]);
    });

    it('selects the previous Monday to Sunday for last week', () => {
      expect(presetRange('lastWeek', new Date(2024, 10, 9, 12))).toEqual([
        { startDate: '2024-10-28', endDate: '2024-11-03' },
      ]);
    });

    it('selects the whole previous month for last month', () => {
      expect(presetRange('lastMonth', new Date(2024, 10, 9, 12))).toEqual([
        { startDate: '2024-10-01', endDate: '2024-10-31' },
      ]);
    });

    it('shifts the whole-month preset to the next full month', () => {
      const [preset] = presetRange('thisMonth', new Date(2024, 10, 9, 12));
      setRange(preset.startDate, preset.endDate);
      const emitted: { startDate: string | null; endDate: string | null }[] = collectEmissions();

      component.shiftRange('next');

      expect(emitted).toEqual([{ startDate: '2024-12-01', endDate: '2024-12-31' }]);
    });

    it('shifts the current-week preset by a week', () => {
      const [preset] = presetRange('thisWeek', new Date(2024, 10, 6, 12));
      setRange(preset.startDate, preset.endDate);
      const emitted: { startDate: string | null; endDate: string | null }[] = collectEmissions();

      component.shiftRange('next');

      expect(emitted).toEqual([{ startDate: '2024-11-11', endDate: '2024-11-17' }]);
    });
  });

  describe('calendar', () => {
    function monthKeys(): string[] {
      return component.visibleMonths().map((month) => month.key);
    }

    function monthByKey(key: string): CalendarMonth {
      const month: CalendarMonth | undefined = component
        .visibleMonths()
        .find((visible) => visible.key === key);
      if (month == null) {
        throw new Error(
          `Month ${key} is not visible; visible months are ${monthKeys().join(', ')}`,
        );
      }

      return month;
    }

    /** Reads a day cell out of one specific grid, so dimmed duplicates cannot be matched by accident. */
    function dayByDate(formattedDate: string, monthKey: string): CalendarDay {
      const day: CalendarDay | undefined = monthByKey(monthKey).days.find(
        (candidate) => candidate.formattedDate === formattedDate,
      );
      if (day == null) {
        throw new Error(`Day ${formattedDate} is not part of the ${monthKey} grid`);
      }

      return day;
    }

    function selectedDates(): string[] {
      const dates = new Set<string>();
      for (const month of component.visibleMonths()) {
        for (const day of month.days) {
          if (day.isSelected) {
            dates.add(day.formattedDate);
          }
        }
      }
      return [...dates].sort();
    }

    function previewDates(): string[] {
      const dates = new Set<string>();
      for (const month of component.visibleMonths()) {
        for (const day of month.days) {
          if (day.isPreview) {
            dates.add(day.formattedDate);
          }
        }
      }
      return [...dates].sort();
    }

    function withToday(date: Date, run: () => void): void {
      vi.useFakeTimers();
      vi.setSystemTime(date);
      try {
        run();
      } finally {
        vi.useRealTimers();
      }
    }

    it('shows exactly two consecutive months, with the anchor month last', () => {
      setRange('2024-11-02', '2024-11-06');

      component.toggle();

      expect(monthKeys()).toEqual(['2024-10-01', '2024-11-01']);
      expect(component.visibleMonths().map((month) => month.days.length)).toEqual([42, 42]);
      expect(component.weekdayLabels.length).toBe(7);
    });

    it('anchors the window on the selected end month, not the start month', () => {
      setRange('2024-09-15', '2024-11-06');

      component.toggle();

      // Anchoring on the start month would show August and September instead.
      expect(monthKeys()).toEqual(['2024-10-01', '2024-11-01']);
    });

    it('starts the grid on the Monday of the week holding the first of the month', () => {
      setRange('2024-11-02', '2024-11-06');

      component.toggle();

      // November 1st, 2024 is a Friday, so the grid opens on Monday October 28th.
      const november: CalendarMonth = monthByKey('2024-11-01');
      expect(november.days[0].formattedDate).toBe('2024-10-28');
      expect(november.days[0].isOutsideMonth).toBe(true);
    });

    it('moves the two-month window one month at a time', () => {
      setRange('2024-11-02', '2024-11-06');
      component.toggle();

      component.shiftVisibleMonths(1);
      expect(monthKeys()).toEqual(['2024-11-01', '2024-12-01']);

      component.shiftVisibleMonths(-2);
      expect(monthKeys()).toEqual(['2024-09-01', '2024-10-01']);
      expect(component.visibleMonths().length).toBe(2);
    });

    it('re-anchors the window to the selection when it is reopened', () => {
      setRange('2024-11-02', '2024-11-06');
      component.toggle();
      component.shiftVisibleMonths(3);
      expect(monthKeys()).toEqual(['2025-01-01', '2025-02-01']);

      component.toggle();
      component.toggle();

      expect(monthKeys()).toEqual(['2024-10-01', '2024-11-01']);
    });

    it('starts a draft on the first click without emitting', () => {
      const emitted: { startDate: string | null; endDate: string | null }[] = collectEmissions();

      component.selectDay('2024-11-02');

      expect(component.startDateDraft()).toBe('2024-11-02');
      expect(emitted).toEqual([]);
    });

    it('completes the range on a later second click', () => {
      component.selectDay('2024-11-02');
      const emitted: { startDate: string | null; endDate: string | null }[] = collectEmissions();

      component.selectDay('2024-11-06');

      expect(emitted).toEqual([{ startDate: '2024-11-02', endDate: '2024-11-06' }]);
      expect(component.startDateDraft()).toBeNull();
    });

    it('completes a single-day range when the same day is clicked twice', () => {
      component.selectDay('2024-11-02');
      const emitted: { startDate: string | null; endDate: string | null }[] = collectEmissions();

      component.selectDay('2024-11-02');

      expect(emitted).toEqual([{ startDate: '2024-11-02', endDate: '2024-11-02' }]);
      expect(component.startDateDraft()).toBeNull();
    });

    it('moves the draft when the second click is earlier', () => {
      component.selectDay('2024-11-06');
      const emitted: { startDate: string | null; endDate: string | null }[] = collectEmissions();

      component.selectDay('2024-11-02');

      expect(emitted).toEqual([]);
      expect(component.startDateDraft()).toBe('2024-11-02');
    });

    describe('hover preview', () => {
      beforeEach(() => {
        setRange('2024-11-01', '2024-11-30');
        component.toggle();
      });

      it('previews the days between the draft and the hovered day', () => {
        component.selectDay('2024-11-10');

        component.hoverDay('2024-11-14');

        expect(previewDates()).toEqual(['2024-11-11', '2024-11-12', '2024-11-13', '2024-11-14']);
      });

      it('does not preview a span that ends before the draft', () => {
        component.selectDay('2024-11-10');

        component.hoverDay('2024-11-04');

        expect(previewDates()).toEqual([]);
      });

      it('does not preview when hovering the draft day itself', () => {
        component.selectDay('2024-11-10');

        component.hoverDay('2024-11-10');

        expect(previewDates()).toEqual([]);
      });

      it('never previews the draft day itself', () => {
        component.selectDay('2024-11-10');

        component.hoverDay('2024-11-14');

        const draft = dayByDate('2024-11-10', '2024-11-01');
        expect(draft).toMatchObject({ isSelected: true, isPreview: false });
      });

      it('does not preview without a draft', () => {
        component.hoverDay('2024-11-14');

        expect(previewDates()).toEqual([]);
      });

      it('keeps dimmed days out of the preview', () => {
        // A span across a month boundary needs the window to reach December: reopen on that end.
        setRange('2024-11-01', '2024-12-20');
        component.toggle();
        component.toggle();

        component.selectDay('2024-11-29');

        component.hoverDay('2024-12-02');

        expect(previewDates()).toEqual(['2024-11-30', '2024-12-01', '2024-12-02']);
        expect(dayByDate('2024-12-02', '2024-11-01')).toMatchObject({
          isOutsideMonth: true,
          isPreview: false,
        });
      });

      it('clears the preview when the pointer leaves the calendar', () => {
        component.selectDay('2024-11-10');
        component.hoverDay('2024-11-14');

        component.clearHoveredDay();

        expect(component.hoveredDate()).toBeNull();
        expect(previewDates()).toEqual([]);
      });

      it('clears the hover when the range is completed', () => {
        component.selectDay('2024-11-02');
        component.hoverDay('2024-11-06');

        component.selectDay('2024-11-06');

        expect(component.hoveredDate()).toBeNull();
        expect(previewDates()).toEqual([]);
      });

      it('previews the range when hovering a day cell', () => {
        component.selectDay('2024-11-10');
        fixture.detectChanges();
        const hovered: HTMLButtonElement = fixture.nativeElement.querySelector(
          '.calendar__day[aria-label="November 14, 2024"]',
        );

        hovered.dispatchEvent(new MouseEvent('mouseenter'));
        fixture.detectChanges();

        expect(hovered.classList.contains('calendar__day--preview')).toBe(true);
        expect(component.hoveredDate()).toBe('2024-11-14');

        const calendar: HTMLElement = fixture.nativeElement.querySelector('.calendar');
        calendar.dispatchEvent(new MouseEvent('mouseleave'));
        fixture.detectChanges();

        expect(hovered.classList.contains('calendar__day--preview')).toBe(false);
      });
    });

    it('highlights every day of the selected range in both months', () => {
      setRange('2024-11-30', '2024-12-02');
      component.toggle();

      expect(selectedDates()).toEqual(['2024-11-30', '2024-12-01', '2024-12-02']);
      const edges: string[] = component
        .visibleMonths()
        .flatMap((month) => month.days)
        .filter((day) => day.isEdge)
        .map((day) => day.formattedDate);
      expect(edges).toEqual(['2024-11-30', '2024-12-02']);
    });

    it('never highlights dimmed days from the neighboring months', () => {
      setRange('2024-11-30', '2024-12-02');
      component.toggle();

      const november = component.visibleMonths()[0].days;
      const december = component.visibleMonths()[1].days;

      expect(november.find((day) => day.formattedDate === '2024-12-01')).toMatchObject({
        isOutsideMonth: true,
        isSelected: false,
        isEdge: false,
      });
      expect(december.find((day) => day.formattedDate === '2024-11-30')).toMatchObject({
        isOutsideMonth: true,
        isSelected: false,
        isEdge: false,
      });
      expect(november.find((day) => day.formattedDate === '2024-11-30')?.isSelected).toBe(true);
      expect(december.find((day) => day.formattedDate === '2024-12-02')?.isSelected).toBe(true);
    });

    it('never rings the dimmed duplicate of today', () => {
      vi.useFakeTimers();
      try {
        // November 5th, 2024 sits in the trailing week of October's grid and in November's own.
        vi.setSystemTime(new Date(2024, 10, 5, 12));
        setRange('2024-11-01', '2024-11-30');
        component.toggle();

        expect(dayByDate('2024-11-05', '2024-11-01')).toMatchObject({
          isToday: true,
          isOutsideMonth: false,
        });
        expect(dayByDate('2024-11-05', '2024-10-01')).toMatchObject({
          isToday: false,
          isOutsideMonth: true,
        });
      } finally {
        vi.useRealTimers();
      }
    });

    it('jumps to the clicked day month when a dimmed day starts the draft', () => {
      setRange('2024-11-02', '2024-11-06');
      component.toggle();

      component.selectDay('2024-12-01', true);

      expect(monthKeys()).toEqual(['2024-11-01', '2024-12-01']);
      expect(component.startDateDraft()).toBe('2024-12-01');
      expect(dayByDate('2024-12-01', '2024-12-01')).toMatchObject({
        isOutsideMonth: false,
        isSelected: true,
      });
      expect(dayByDate('2024-12-01', '2024-11-01')).toMatchObject({
        isOutsideMonth: true,
        isSelected: false,
      });
    });

    it('jumps to the clicked day month when a dimmed day completes the range', () => {
      setRange('2024-11-02', '2024-11-06');
      component.toggle();
      component.selectDay('2024-11-20');
      const emitted: { startDate: string | null; endDate: string | null }[] = collectEmissions();

      component.selectDay('2025-01-02', true);

      expect(emitted).toEqual([{ startDate: '2024-11-20', endDate: '2025-01-02' }]);
      expect(monthKeys()).toEqual(['2024-12-01', '2025-01-01']);
    });

    it('shows only the draft while it is pending, hiding the old range', () => {
      setRange('2024-11-02', '2024-11-06');
      component.toggle();

      component.selectDay('2024-11-03');

      expect(selectedDates()).toEqual(['2024-11-03']);
    });

    it('clears a pending draft when the popup closes', () => {
      setRange('2024-11-02', '2024-11-06');
      component.toggle();
      component.selectDay('2024-11-03');

      component.toggle();

      expect(component.startDateDraft()).toBeNull();
    });

    it('clears a pending draft when a preset is selected and anchors on its end month', () => {
      withToday(new Date(2024, 10, 9, 12), () => {
        component.selectDay('2024-11-03');

        component.setPreset('today');

        expect(component.startDateDraft()).toBeNull();
        expect(monthKeys()).toEqual(['2024-10-01', '2024-11-01']);
      });
    });

    it('anchors the window on the preset end month when the preset spans two months', () => {
      withToday(new Date(2024, 11, 31, 12), () => {
        const emitted: { startDate: string | null; endDate: string | null }[] = collectEmissions();

        component.setPreset('thisWeek');

        expect(emitted).toEqual([{ startDate: '2024-12-30', endDate: '2025-01-05' }]);
        expect(monthKeys()).toEqual(['2024-12-01', '2025-01-01']);
      });
    });

    it('falls back to the current month when there is no end date to anchor on', () => {
      withToday(new Date(2024, 10, 9, 12), () => {
        setRange('2024-09-15', null);

        component.toggle();

        // The start date is ignored on purpose: only the end date anchors the window.
        expect(monthKeys()).toEqual(['2024-10-01', '2024-11-01']);
      });
    });

    it('clears a pending draft when the range is shifted', () => {
      setRange('2024-11-02', '2024-11-06');
      component.selectDay('2024-11-03');

      component.shiftRange('next');

      expect(component.startDateDraft()).toBeNull();
      expect(monthKeys()).toEqual(['2024-10-01', '2024-11-01']);
    });

    it('anchors the window on the shifted end month across a month boundary', () => {
      setRange('2024-01-28', '2024-01-31');
      const emitted: { startDate: string | null; endDate: string | null }[] = collectEmissions();

      component.shiftRange('next');

      expect(emitted).toEqual([{ startDate: '2024-02-01', endDate: '2024-02-04' }]);
      expect(monthKeys()).toEqual(['2024-01-01', '2024-02-01']);
    });

    it('renders both months, weekday headers and day cells', () => {
      fixture.detectChanges();
      const calendar: HTMLElement = fixture.nativeElement.querySelector('.calendar');

      expect(calendar.querySelectorAll('.calendar__day').length).toBe(84);
      expect(calendar.querySelectorAll('.calendar__weekday').length).toBe(14);
      expect(calendar.querySelectorAll('.calendar__month-label').length).toBe(2);
      expect(calendar.querySelectorAll('.calendar__nav').length).toBe(2);
    });

    it('starts a draft when a day cell is clicked', () => {
      setRange('2024-11-02', '2024-11-06');
      component.toggle();
      fixture.detectChanges();

      const cell: HTMLButtonElement = fixture.nativeElement.querySelector(
        '.calendar__day[aria-label="November 2, 2024"]',
      );
      cell.click();

      expect(component.startDateDraft()).toBe('2024-11-02');
    });

    it('jumps to the clicked month when a dimmed day cell is clicked', () => {
      setRange('2024-11-02', '2024-11-06');
      component.toggle();
      fixture.detectChanges();

      const cell: HTMLButtonElement = fixture.nativeElement.querySelector(
        '.calendar__day--outside[aria-label="December 5, 2024"]',
      );
      cell.click();
      fixture.detectChanges();

      expect(component.startDateDraft()).toBe('2024-12-05');
      expect(monthKeys()).toEqual(['2024-11-01', '2024-12-01']);
    });
  });
});
