import { provideHttpClient } from '@angular/common/http';
import { provideHttpClientTesting } from '@angular/common/http/testing';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { MigrationSettings } from './migration-settings';

describe('MigrationSettings', () => {
  let component: MigrationSettings;
  let fixture: ComponentFixture<MigrationSettings>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [MigrationSettings],
      providers: [provideHttpClient(), provideHttpClientTesting()],
    }).compileComponents();

    fixture = TestBed.createComponent(MigrationSettings);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('opens the import modal from the import button', () => {
    const button: HTMLButtonElement = fixture.nativeElement.querySelector(
      '.migration__import-button',
    );
    expect(button.textContent?.trim()).toBe('Import from Clockify');

    button.click();
    fixture.detectChanges();

    expect(component.isImportModalOpen()).toBe(true);
    expect(fixture.nativeElement.querySelector('app-import-modal')).toBeTruthy();
  });
});
