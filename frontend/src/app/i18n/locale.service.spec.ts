import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { clearTranslations } from '@angular/localize';
import { LOCALE_STORAGE_KEY } from './locale.constants';
import { LocaleService } from './locale.service';

describe('LocaleService', () => {
  let httpMock: HttpTestingController;

  beforeEach(() => {
    localStorage.clear();
    TestBed.configureTestingModule({
      providers: [provideHttpClient(), provideHttpClientTesting()],
    });
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    clearTranslations();
    localStorage.clear();
    httpMock.verify();
  });

  it('loads the translations for the active locale', async () => {
    localStorage.setItem(LOCALE_STORAGE_KEY, 'es-419');
    const service: LocaleService = TestBed.inject(LocaleService);

    const initialization: Promise<void> = service.initialize();

    httpMock.expectOne('locale/messages.es-419.json').flush({
      locale: 'es-419',
      translations: { greeting: 'Hola' },
    });
    await initialization;

    expect($localize`:@@greeting:Hello`).toBe('Hola');
  });

  it('keeps the source strings when translations cannot be loaded', async () => {
    localStorage.setItem(LOCALE_STORAGE_KEY, 'es-419');
    const service: LocaleService = TestBed.inject(LocaleService);

    const initialization: Promise<void> = service.initialize();

    httpMock
      .expectOne('locale/messages.es-419.json')
      .flush({ message: 'not found' }, { status: 404, statusText: 'Not Found' });
    await initialization;

    expect($localize`:@@greeting:Hello`).toBe('Hello');
  });
});
