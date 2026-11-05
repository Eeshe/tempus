/// <reference types="@angular/localize" />

import { registerLocaleData } from '@angular/common';
import localeEs419 from '@angular/common/locales/es-419';
import { bootstrapApplication } from '@angular/platform-browser';
import { appConfig } from './app/app.config';
import { App } from './app/app';
import { resolveInitialLocale, SupportedLocale } from './app/i18n/locale.constants';

const initialLocale: SupportedLocale = resolveInitialLocale();

if (initialLocale === 'es-419') {
  registerLocaleData(localeEs419, 'es-419');
}

$localize.locale = initialLocale;
document.documentElement.lang = initialLocale;

bootstrapApplication(App, appConfig)
  .catch((err) => console.error(err));
