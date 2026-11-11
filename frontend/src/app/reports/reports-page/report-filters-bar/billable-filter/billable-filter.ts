import { Component, inject, input, output } from '@angular/core';
import { CurrencyService } from '../../../../services/currency.service';
import { PopupSelectorBase } from '../../../../shared/selector/popup-selector-base';

@Component({
  imports: [],
  selector: 'app-billable-filter',
  styleUrl: './billable-filter.css',
  templateUrl: './billable-filter.html',
})
export class BillableFilter extends PopupSelectorBase {
  private readonly currencyService: CurrencyService = inject(CurrencyService);

  readonly isBillable = input<boolean | null>(null);

  readonly symbol = this.currencyService.symbol;

  readonly isBillableChangeEvent = output<boolean | null>();

  select(value: boolean | null): void {
    this.isBillableChangeEvent.emit(value);
    this.closeSelector();
  }

  isSelected(value: boolean | null): boolean {
    return this.isBillable() === value;
  }
}
