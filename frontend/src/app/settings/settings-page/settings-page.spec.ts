import { TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';
import { RouterTestingHarness } from '@angular/router/testing';
import { SettingsPage } from './settings-page';

describe('SettingsPage', () => {
  beforeEach(async () => {
    await TestBed.configureTestingModule({
      providers: [provideRouter([{ path: 'settings', component: SettingsPage }])],
    }).compileComponents();
  });

  it('should create', async () => {
    const harness = await RouterTestingHarness.create('/settings');

    expect(harness.routeNativeElement).toBeTruthy();
  });

  it('renders links to the settings sections', async () => {
    const harness = await RouterTestingHarness.create('/settings');
    const links: HTMLAnchorElement[] = Array.from(
      harness.routeNativeElement!.querySelectorAll('.settings__nav-item'),
    );

    expect(links.map((link) => link.textContent?.trim())).toEqual(['Language', 'Migration']);
    expect(links.map((link) => link.getAttribute('href'))).toEqual([
      '/settings/language',
      '/settings/migration',
    ]);
  });
});
