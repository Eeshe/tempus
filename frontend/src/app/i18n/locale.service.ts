import { HttpClient } from '@angular/common/http';
import { inject, Service, Signal, signal } from '@angular/core';
import { loadTranslations } from '@angular/localize';
import { firstValueFrom, Observable, of } from 'rxjs';
import { catchError, tap } from 'rxjs/operators';
import {
  DEFAULT_LOCALE,
  LOCALE_STORAGE_KEY,
  resolveInitialLocale,
  SUPPORTED_LOCALES,
  SupportedLocale,
} from './locale.constants';

interface TranslationFile {
  readonly locale: string;
  readonly translations: Record<string, string>;
}

@Service()
export class LocaleService {
  private readonly http: HttpClient = inject(HttpClient);

  private readonly _locale = signal<SupportedLocale>(resolveInitialLocale());

  readonly locale: Signal<SupportedLocale> = this._locale.asReadonly();
  readonly locales = SUPPORTED_LOCALES;

  initialize(): Promise<void> {
    const locale: SupportedLocale = this._locale();
    $localize.locale = locale;
    this.updateDocumentLanguage(locale);

    if (locale === DEFAULT_LOCALE) {
      return Promise.resolve();
    }
    const translationRequest: Observable<TranslationFile> = this.http
      .get<TranslationFile>(`locale/messages.${locale}.json`)
      .pipe(
        tap((translationFile: TranslationFile) => loadTranslations(translationFile.translations)),
        catchError((error: unknown) => {
          console.error(`Could not load translations for locale "${locale}".`, error);
          return of<TranslationFile>({ locale, translations: {} });
        }),
      );
    return firstValueFrom(translationRequest).then(() => undefined);
  }

  setLocale(locale: SupportedLocale): void {
    if (locale === this._locale()) {
      return;
    }
    this.persistLocale(locale);
    window.location.reload();
  }

  persistLocale(locale: SupportedLocale): void {
    this._locale.set(locale);
    $localize.locale = locale;
    this.updateDocumentLanguage(locale);
    try {
      localStorage.setItem(LOCALE_STORAGE_KEY, locale);
    } catch {
      // Storage can be unavailable in private browsing or restricted environments.
    }
  }

  private updateDocumentLanguage(locale: SupportedLocale): void {
    if (typeof document !== 'undefined') {
      document.documentElement.lang = locale;
    }
  }
}
