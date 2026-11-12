import { ChangeDetectionStrategy, Component, computed, inject, input } from '@angular/core';
import { CurrencyService } from '../../../../services/currency.service';
import { DurationPipe } from '../../../../shared/pipes/duration.pipe';
import { formatYYYYMMDDDate } from '../../../../shared/util/date.util';
import { toHHmmTime } from '../../../../shared/util/time.util';
import { ReportTimeEntry } from '../../../models/report.model';

@Component({
  selector: 'app-report-time-entry-row',
  imports: [DurationPipe],
  templateUrl: './report-time-entry-row.html',
  changeDetection: ChangeDetectionStrategy.Eager,
  styleUrl: './report-time-entry-row.css',
})
export class ReportTimeEntryRow {
  private readonly currencyService: CurrencyService = inject(CurrencyService);

  readonly symbol = this.currencyService.symbol;

  readonly timeEntry = input.required<ReportTimeEntry>();
  readonly projectColor = input.required<string>();
  readonly isArchived = input.required<boolean>();

  readonly hasDescription = computed<boolean>(() => {
    const description: string | null = this.timeEntry().description;

    return description != null && description.trim() !== '';
  });
  readonly formattedDate = computed<string>(() => formatYYYYMMDDDate(new Date(this.timeEntry().startTime)));
  readonly formattedStartTime = computed<string>(() => toHHmmTime(new Date(this.timeEntry().startTime)));
  readonly formattedEndTime = computed<string>(() => {
    const endTime: string | null = this.timeEntry().endTime;

    return endTime == null ? '' : toHHmmTime(new Date(endTime));
  });
}
