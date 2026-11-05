import { provideHttpClient } from '@angular/common/http';
import { provideHttpClientTesting } from '@angular/common/http/testing';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { vi } from 'vitest';
import { LocaleService } from '../../i18n/locale.service';
import { LanguageSettings } from './language-settings';

describe('LanguageSettings', () => {
  let fixture: ComponentFixture<LanguageSettings>;
  let localeService: LocaleService;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [LanguageSettings],
      providers: [provideHttpClient(), provideHttpClientTesting()],
    }).compileComponents();

    localeService = TestBed.inject(LocaleService);
    fixture = TestBed.createComponent(LanguageSettings);
    await fixture.whenStable();
  });

  afterEach(() => {
    localStorage.clear();
  });

  it('renders both supported languages', () => {
    const options: HTMLInputElement[] = Array.from(
      fixture.nativeElement.querySelectorAll('.language__option input'),
    );

    expect(options.map((option) => option.value)).toEqual(['en-US', 'es-419']);
  });

  it('requests a locale change when an option is selected', () => {
    const setLocaleSpy = vi.spyOn(localeService, 'setLocale').mockImplementation(() => {});
    const spanishOption: HTMLInputElement = fixture.nativeElement.querySelector(
      '.language__option input[value="es-419"]',
    );

    spanishOption.click();

    expect(setLocaleSpy).toHaveBeenCalledWith('es-419');
  });
});
