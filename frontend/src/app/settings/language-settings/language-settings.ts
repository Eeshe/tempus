import { Component, inject } from '@angular/core';
import { LocaleService } from '../../i18n/locale.service';
import { LocaleOption, SupportedLocale } from '../../i18n/locale.constants';

@Component({
  selector: 'app-language-settings',
  styleUrl: './language-settings.css',
  templateUrl: './language-settings.html',
})
export class LanguageSettings {
  private readonly localeService: LocaleService = inject(LocaleService);

  readonly locales: readonly LocaleOption[] = this.localeService.locales;
  readonly selectedLocale = this.localeService.locale;

  selectLocale(locale: SupportedLocale): void {
    this.localeService.setLocale(locale);
  }
}
