import { Component, input, output } from '@angular/core';
import { PopupSelectorBase } from '../../../../shared/selector/popup-selector-base';

@Component({
  imports: [],
  selector: 'app-billable-filter',
  styleUrl: './billable-filter.css',
  templateUrl: './billable-filter.html',
})
export class BillableFilter extends PopupSelectorBase {
  readonly isBillable = input<boolean | null>(null);

  readonly isBillableChangeEvent = output<boolean | null>();

  select(value: boolean | null): void {
    this.isBillableChangeEvent.emit(value);
  }

  isSelected(value: boolean | null): boolean {
    return this.isBillable() === value;
  }
}
