import { provideHttpClient } from '@angular/common/http';
import { provideHttpClientTesting } from '@angular/common/http/testing';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { LocalizationSettings } from './localization-settings';

describe('LocalizationSettings', () => {
  let fixture: ComponentFixture<LocalizationSettings>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [LocalizationSettings],
      providers: [provideHttpClient(), provideHttpClientTesting()],
    }).compileComponents();

    fixture = TestBed.createComponent(LocalizationSettings);
    await fixture.whenStable();
  });

  afterEach(() => {
    localStorage.clear();
  });

  it('renders the language and currency sections', () => {
    const languageOptions: HTMLInputElement[] = Array.from(
      fixture.nativeElement.querySelectorAll('.language__option input'),
    );
    const currencyTrigger: HTMLElement | null = fixture.nativeElement.querySelector(
      'app-currency-settings .selector__trigger',
    );

    expect(languageOptions.map((option) => option.value)).toEqual(['en-US', 'es-419']);
    expect(currencyTrigger).toBeTruthy();
  });
});
