import { CanDeactivateFn } from '@angular/router';
import { AppearanceSettings } from './appearance-settings';

/**
 * Reverts the live color preview (after confirmation) when the user navigates away from the
 * appearance tab with unsaved changes.
 */
export const unsavedThemeChangesGuard: CanDeactivateFn<AppearanceSettings> = (
  component: AppearanceSettings,
): boolean | Promise<boolean> => {
  if (!component.hasUnsavedChanges()) {
    return true;
  }
  return component.confirmDiscard();
};
