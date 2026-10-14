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

  it('renders a link to the migration section', async () => {
    const harness = await RouterTestingHarness.create('/settings');
    const link: HTMLAnchorElement = harness.routeNativeElement!.querySelector(
      '.settings__nav-item',
    )!;

    expect(link.textContent?.trim()).toBe('Migration');
    expect(link.getAttribute('href')).toBe('/settings/migration');
  });
});
