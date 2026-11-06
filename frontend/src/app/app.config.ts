import {
  ApplicationConfig,
  inject,
  LOCALE_ID,
  provideAppInitializer,
  provideBrowserGlobalErrorListeners,
} from '@angular/core';
import { provideRouter, TitleStrategy, withViewTransitions } from '@angular/router';

import { provideHttpClient, withInterceptors, withXhr } from '@angular/common/http';
import { routes } from './app.routes';
import { LocaleService } from './i18n/locale.service';
import { LocalizedTitleStrategy } from './i18n/localized-title.strategy';
import { authErrorInterceptor } from './interceptors/auth-error.interceptor';
import { AuthService } from './services/auth.service';
import { ThemeService } from './services/theme.service';

export const appConfig: ApplicationConfig = {
  providers: [
    provideBrowserGlobalErrorListeners(),
    provideAppInitializer(() => inject(LocaleService).initialize()),
    provideAppInitializer(() => inject(AuthService).checkAuthentication()),
    provideAppInitializer(() => inject(ThemeService).initialize()),
    provideRouter(routes, withViewTransitions()),
    provideHttpClient(withInterceptors([authErrorInterceptor]), withXhr()),
    { provide: LOCALE_ID, useFactory: () => inject(LocaleService).locale() },
    { provide: TitleStrategy, useClass: LocalizedTitleStrategy },
  ],
};
