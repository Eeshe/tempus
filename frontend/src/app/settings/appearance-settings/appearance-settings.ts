import {
  ChangeDetectionStrategy,
  Component,
  computed,
  HostListener,
  inject,
  linkedSignal,
  Signal,
  signal,
} from '@angular/core';
import { AuthService } from '../../services/auth.service';
import { DEFAULT_THEME, ThemeService } from '../../services/theme.service';
import { DeleteConfirmationModal } from '../../shared/delete-confirmation-modal/delete-confirmation-modal';
import { ColorInput } from '../../shared/input/color-input/color-input';
import { Theme, ThemeField, UserSettings } from '../../user/models/user.model';
import { UserSettingsService } from '../services/user-settings.service';

interface ThemeFieldOption {
  readonly field: ThemeField;
  readonly id: string;
  readonly label: string;
}

@Component({
  imports: [ColorInput, DeleteConfirmationModal],
  selector: 'app-appearance-settings',
  styleUrl: './appearance-settings.css',
  templateUrl: './appearance-settings.html',
  changeDetection: ChangeDetectionStrategy.Eager,
})
export class AppearanceSettings {
  private readonly authService: AuthService = inject(AuthService);
  private readonly themeService: ThemeService = inject(ThemeService);
  private readonly userSettingsService: UserSettingsService = inject(UserSettingsService);

  readonly themeFields: readonly ThemeFieldOption[] = [
    {
      field: 'background',
      id: 'themeBackground',
      label: $localize`:Appearance settings|Background color:Background`,
    },
    {
      field: 'surface',
      id: 'themeSurface',
      label: $localize`:Appearance settings|Surface color:Surface`,
    },
    {
      field: 'text',
      id: 'themeText',
      label: $localize`:Appearance settings|Text color:Text`,
    },
    {
      field: 'accent',
      id: 'themeAccent',
      label: $localize`:Appearance settings|Accent color:Accent`,
    },
    {
      field: 'danger',
      id: 'themeDanger',
      label: $localize`:Appearance settings|Danger color:Danger`,
    },
  ];

  readonly noPresetColors: string[] = [];

  readonly discardHeader: string = $localize`:Appearance settings|Unsaved changes dialog question:Discard unsaved color changes?`;
  readonly discardConfirmLabel: string = $localize`:Appearance settings|Discard changes button:Discard`;

  private readonly savedTheme = computed<Theme>(() => ({
    ...(this.authService.currentUser()?.settings.theme ?? DEFAULT_THEME),
  }));

  readonly draft = linkedSignal<Theme>(() => ({ ...this.savedTheme() }));

  readonly isDirty: Signal<boolean> = computed<boolean>(
    () => !this.themeService.themeEquals(this.draft(), this.savedTheme()),
  );

  readonly isDefaultTheme: Signal<boolean> = computed<boolean>(() =>
    this.themeService.themeEquals(this.draft(), DEFAULT_THEME),
  );

  readonly isSaving = signal<boolean>(false);
  readonly saveError = signal<string | null>(null);
  readonly isDiscardConfirmationOpen = signal<boolean>(false);

  private pendingDiscardResolve: ((discard: boolean) => void) | null = null;

  updateColor(field: ThemeField, color: string): void {
    const nextDraft: Theme = { ...this.draft(), [field]: color };

    this.draft.set(nextDraft);
    this.saveError.set(null);
    this.themeService.previewTheme(nextDraft);
  }

  apply(): void {
    if (!this.isDirty() || this.isSaving()) {
      return;
    }
    this.isSaving.set(true);
    this.saveError.set(null);

    this.userSettingsService.patchTheme(this.draft()).subscribe({
      next: (settings: UserSettings) => {
        this.authService.setTheme(settings.theme);
        this.isSaving.set(false);
      },
      error: () => {
        this.saveError.set(
          $localize`:Appearance settings|Error shown when the theme could not be saved:Your colors could not be saved. Try again`,
        );
        this.isSaving.set(false);
      },
    });
  }

  reset(): void {
    const savedTheme: Theme = this.savedTheme();

    this.draft.set({ ...savedTheme });
    this.saveError.set(null);
    this.themeService.previewTheme(savedTheme);
  }

  restoreDefaultColors(): void {
    const defaultTheme: Theme = { ...DEFAULT_THEME };

    this.draft.set(defaultTheme);
    this.saveError.set(null);
    this.themeService.previewTheme(defaultTheme);
  }

  hasUnsavedChanges(): boolean {
    return this.isDirty();
  }

  /**
   * Used by the canDeactivate guard: resolves true only after the user confirms discarding the
   * live preview.
   */
  confirmDiscard(): Promise<boolean> {
    if (!this.isDirty()) {
      return Promise.resolve(true);
    }
    this.isDiscardConfirmationOpen.set(true);

    return new Promise<boolean>((resolve: (discard: boolean) => void) => {
      this.pendingDiscardResolve = resolve;
    });
  }

  handleDiscardConfirmation(discard: boolean): void {
    this.isDiscardConfirmationOpen.set(false);
    const resolve: ((discard: boolean) => void) | null = this.pendingDiscardResolve;
    this.pendingDiscardResolve = null;
    if (discard) {
      this.reset();
    }
    resolve?.(discard);
  }

  @HostListener('window:beforeunload', ['$event'])
  warnAboutUnsavedChanges(event: BeforeUnloadEvent): void {
    if (!this.isDirty()) {
      return;
    }
    event.preventDefault();
  }
}
