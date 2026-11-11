import { getCurrencySymbol } from '@angular/common';
import { computed, inject, LOCALE_ID, Service, Signal } from '@angular/core';
import { DEFAULT_CURRENCY } from '../shared/util/currency.util';
import { AuthService } from './auth.service';

/**
 * Exposes the currency configured in the user settings as a signal, so every money display and
 * billable marker re-renders as soon as the setting changes either locally or through /me.
 */
@Service()
export class CurrencyService {
  private readonly authService: AuthService = inject(AuthService);
  private readonly localeId: string = inject(LOCALE_ID);

  readonly currency: Signal<string> = computed<string>(
    () => this.authService.currentUser()?.settings.currency ?? DEFAULT_CURRENCY,
  );

  /** The narrow symbol ("$", "€"): the wide one is the ISO code in locales such as es-419. */
  readonly symbol: Signal<string> = computed<string>(() =>
    getCurrencySymbol(this.currency(), 'narrow', this.localeId),
  );
}
