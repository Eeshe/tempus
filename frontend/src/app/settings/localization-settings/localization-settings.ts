import { Component } from '@angular/core';
import { LanguageSettings } from '../language-settings/language-settings';
import { CurrencySettings } from '../currency-settings/currency-settings';

@Component({
  imports: [LanguageSettings, CurrencySettings],
  selector: 'app-localization-settings',
  styleUrl: './localization-settings.css',
  templateUrl: './localization-settings.html',
})
export class LocalizationSettings {}
