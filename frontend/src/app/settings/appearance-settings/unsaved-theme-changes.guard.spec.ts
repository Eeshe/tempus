import { ActivatedRouteSnapshot, RouterStateSnapshot } from '@angular/router';
import { AppearanceSettings } from './appearance-settings';
import { unsavedThemeChangesGuard } from './unsaved-theme-changes.guard';

function createComponent(options: {
  hasUnsavedChanges: boolean;
  confirmResult?: boolean;
}): AppearanceSettings {
  return {
    hasUnsavedChanges: () => options.hasUnsavedChanges,
    confirmDiscard: () => Promise.resolve(options.confirmResult ?? false),
  } as unknown as AppearanceSettings;
}

const route: ActivatedRouteSnapshot = {} as ActivatedRouteSnapshot;
const state: RouterStateSnapshot = {} as RouterStateSnapshot;

describe('unsavedThemeChangesGuard', () => {
  it('allows navigation without prompting when there are no unsaved changes', () => {
    const result = unsavedThemeChangesGuard(
      createComponent({ hasUnsavedChanges: false }),
      route,
      state,
      state,
    );

    expect(result).toBe(true);
  });

  it('asks the component to confirm when there are unsaved changes', async () => {
    const result = unsavedThemeChangesGuard(
      createComponent({ hasUnsavedChanges: true, confirmResult: true }),
      route,
      state,
      state,
    );

    await expect(result).resolves.toBe(true);
  });

  it('blocks navigation when the user keeps editing', async () => {
    const result = unsavedThemeChangesGuard(
      createComponent({ hasUnsavedChanges: true, confirmResult: false }),
      route,
      state,
      state,
    );

    await expect(result).resolves.toBe(false);
  });
});
