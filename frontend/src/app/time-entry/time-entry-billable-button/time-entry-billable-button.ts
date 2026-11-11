import { Component, inject, input } from '@angular/core';
import { CurrencyService } from '../../services/currency.service';
import { TimeEntry } from '../models/time-entry.model';
import { TimeEntryStore } from '../stores/time-entry.store';

@Component({
  imports: [],
  selector: 'app-time-entry-billable-button',
  styleUrl: './time-entry-billable-button.css',
  templateUrl: './time-entry-billable-button.html',
})
export class TimeEntryBillableButton {
  private readonly timeEntryStore: TimeEntryStore = inject(TimeEntryStore);
  private readonly currencyService: CurrencyService = inject(CurrencyService);

  readonly timeEntry = input.required<TimeEntry>();

  readonly symbol = this.currencyService.symbol;

  toggleBillable(): void {
    const newBillableStatus: boolean = !this.timeEntry().isBillable;
    this.timeEntry().isBillable = newBillableStatus;

    this.timeEntryStore.patchBillable(this.timeEntry(), newBillableStatus);
  }
}
