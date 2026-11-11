import { Component, computed, inject, LOCALE_ID, signal } from '@angular/core';
import { AuthService } from '../../services/auth.service';
import { CurrencyService } from '../../services/currency.service';
import { SearchInput } from '../../shared/input/search-input/search-input';
import { PopupSelectorBase } from '../../shared/selector/popup-selector-base';
import { CurrencyOption, getCurrencyOptions } from '../../shared/util/currency.util';
import { normalizeForSearch } from '../../shared/util/string.util';
import { UserSettings } from '../../user/models/user.model';
import { UserSettingsService } from '../services/user-settings.service';

interface CurrencySearchEntry {
  readonly option: CurrencyOption;
  readonly haystack: string;
}

@Component({
  imports: [SearchInput],
  selector: 'app-currency-settings',
  styleUrl: './currency-settings.css',
  templateUrl: './currency-settings.html',
})
export class CurrencySettings extends PopupSelectorBase {
  private readonly authService: AuthService = inject(AuthService);
  private readonly currencyService: CurrencyService = inject(CurrencyService);
  private readonly userSettingsService: UserSettingsService = inject(UserSettingsService);

  readonly currency = this.currencyService.currency;
  readonly symbol = this.currencyService.symbol;
  readonly options: readonly CurrencyOption[] = getCurrencyOptions(inject(LOCALE_ID));

  /** Pre-folded code + name of every currency, so typing "dolar" or "eur" matches the same list. */
  private readonly searchIndex: readonly CurrencySearchEntry[] = this.options.map(
    (option: CurrencyOption) => ({
      option: option,
      haystack: normalizeForSearch(`${option.code} ${option.name}`),
    }),
  );

  readonly filteredOptions = computed<CurrencyOption[]>(() => {
    const query: string = normalizeForSearch(this.searchQuery());
    if (query === '') {
      return [...this.options];
    }
    return this.searchIndex
      .filter((entry: CurrencySearchEntry) => entry.haystack.includes(query))
      .map((entry: CurrencySearchEntry) => entry.option);
  });

  readonly searchPlaceholder: string = $localize`:Currency settings|Search field placeholder:Search currencies`;

  readonly isSaving = signal<boolean>(false);
  readonly saveError = signal<string | null>(null);

  selectCurrency(currencyCode: string): void {
    this.closeSelector();
    if (currencyCode === this.currency() || this.isSaving()) {
      return;
    }
    this.isSaving.set(true);
    this.saveError.set(null);

    this.userSettingsService.patchCurrency(currencyCode).subscribe({
      next: (settings: UserSettings) => {
        this.authService.setCurrency(settings.currency);
        this.isSaving.set(false);
      },
      error: () => {
        this.saveError.set(
          $localize`:Currency settings|Error shown when the currency could not be saved:Your currency could not be saved. Try again`,
        );
        this.isSaving.set(false);
      },
    });
  }
}
