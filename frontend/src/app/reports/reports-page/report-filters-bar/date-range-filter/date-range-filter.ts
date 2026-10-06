import { Component, input, output, signal } from '@angular/core';
import { DateInput } from '../../../../shared/input/date-input/date-input';
import { PopupSelectorBase } from '../../../../shared/selector/popup-selector-base';
import { formatYYYYMMDDDate } from '../../../../shared/util/date.util';

@Component({
  imports: [DateInput],
  selector: 'app-date-range-filter',
  styleUrl: './date-range-filter.css',
  templateUrl: './date-range-filter.html',
})
export class DateRangeFilter extends PopupSelectorBase {
  readonly startDate = input<string | null>(null);
  readonly endDate = input<string | null>(null);

  readonly datesChangeEvent = output<{ startDate: string | null, endDate: string | null }>();

  readonly startDateInvalid = signal<boolean>(false);
  readonly endDateInvalid = signal<boolean>(false);

  onStartDateChange(value: string | null): void {
    this.datesChangeEvent.emit({ startDate: value, endDate: this.endDate() });

    this.validateDates(value, this.endDate());
  }

  onEndDateChange(value: string | null): void {
    this.datesChangeEvent.emit({ startDate: this.startDate(), endDate: value });

    this.validateDates(this.startDate(), value);
  }

  onStartDateTouch(): void { }
  onEndDateTouch(): void { }

  private validateDates(start: string | null, end: string | null): void {
    if (start && end) {
      this.startDateInvalid.set(start > end);
      this.endDateInvalid.set(end < start);
    } else {
      this.startDateInvalid.set(false);
      this.endDateInvalid.set(false);
    }
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
        end = new Date(year, month, day);
        break;
      case 'lastWeek':
        start = new Date(year, month, day - mondayOffset - 7);
        end = new Date(year, month, day - mondayOffset - 1);
        break;
      case 'thisMonth':
        start = new Date(year, month, 1);
        end = new Date(year, month, day);
        break;
      case 'lastMonth':
        start = new Date(year, month - 1, 1);
        end = new Date(year, month, 0);
        break;
      case 'thisYear':
        start = new Date(year, 0, 1);
        end = new Date(year, month, day);
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

    this.datesChangeEvent.emit({ startDate: startStr, endDate: endStr });

    this.validateDates(startStr, endStr);
  }
}
