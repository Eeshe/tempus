package me.eeshe.tempus.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Currency;
import java.util.NoSuchElementException;
import java.util.Optional;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openapitools.jackson.nullable.JsonNullable;

import me.eeshe.tempus.entity.Theme;
import me.eeshe.tempus.entity.UserSettings;
import me.eeshe.tempus.repository.UserSettingsRepository;
import me.eeshe.tempus.request.PatchUserSettingsRequest;
import me.eeshe.tempus.request.ThemePatch;
import me.eeshe.tempus.support.EntityTestBase;

@ExtendWith(MockitoExtension.class)
public class UserSettingsServiceImplTest extends EntityTestBase {
    private static final Currency EUR = Currency.getInstance("EUR");

    @Mock
    private UserSettingsRepository userSettingsRepository;

    @InjectMocks
    private UserSettingsServiceImpl userSettingsService;

    @Nested
    class PatchUserSettings {

        @Test
        void patchesOnlyProvidedColors() {
            final UserSettings userSettings = createUser(USER_ID).getSettings();
            final PatchUserSettingsRequest patchUserSettingsRequest = new PatchUserSettingsRequest(
                    new ThemePatch(
                            JsonNullable.of("#000000"),
                            JsonNullable.undefined(),
                            JsonNullable.undefined(),
                            JsonNullable.of("#FFFFFF"),
                            JsonNullable.undefined()),
                    JsonNullable.undefined());

            when(userSettingsRepository.findByUserId(USER_ID)).thenReturn(Optional.of(userSettings));
            when(userSettingsRepository.save(userSettings)).thenReturn(userSettings);

            final UserSettings patchedUserSettings = userSettingsService.patchUserSettings(
                    USER_ID,
                    patchUserSettingsRequest);

            assertThat(patchedUserSettings.getTheme()).isEqualTo(new Theme(
                    "#000000",
                    Theme.DEFAULT_SURFACE,
                    Theme.DEFAULT_TEXT,
                    "#FFFFFF",
                    Theme.DEFAULT_DANGER));
            verify(userSettingsRepository).save(userSettings);
        }

        @Test
        void patchesAllColors() {
            final UserSettings userSettings = createUser(USER_ID).getSettings();
            final PatchUserSettingsRequest patchUserSettingsRequest = new PatchUserSettingsRequest(
                    new ThemePatch(
                            JsonNullable.of("#000001"),
                            JsonNullable.of("#000002"),
                            JsonNullable.of("#000003"),
                            JsonNullable.of("#000004"),
                            JsonNullable.of("#000005")),
                    JsonNullable.undefined());

            when(userSettingsRepository.findByUserId(USER_ID)).thenReturn(Optional.of(userSettings));
            when(userSettingsRepository.save(userSettings)).thenReturn(userSettings);

            final UserSettings patchedUserSettings = userSettingsService.patchUserSettings(
                    USER_ID,
                    patchUserSettingsRequest);

            assertThat(patchedUserSettings.getTheme()).isEqualTo(new Theme(
                    "#000001",
                    "#000002",
                    "#000003",
                    "#000004",
                    "#000005"));
        }

        @Test
        void patchesCurrency() {
            final UserSettings userSettings = createUser(USER_ID).getSettings();
            final PatchUserSettingsRequest patchUserSettingsRequest = new PatchUserSettingsRequest(
                    null,
                    JsonNullable.of(EUR));

            when(userSettingsRepository.findByUserId(USER_ID)).thenReturn(Optional.of(userSettings));
            when(userSettingsRepository.save(userSettings)).thenReturn(userSettings);

            final UserSettings patchedUserSettings = userSettingsService.patchUserSettings(
                    USER_ID,
                    patchUserSettingsRequest);

            assertThat(patchedUserSettings.getCurrency()).isEqualTo(EUR);
            assertThat(patchedUserSettings.getTheme()).isEqualTo(Theme.DEFAULT);
            verify(userSettingsRepository).save(userSettings);
        }

        @Test
        void patchesCurrencyAndThemeTogether() {
            final UserSettings userSettings = createUser(USER_ID).getSettings();
            final PatchUserSettingsRequest patchUserSettingsRequest = new PatchUserSettingsRequest(
                    new ThemePatch(
                            JsonNullable.of("#000000"),
                            JsonNullable.undefined(),
                            JsonNullable.undefined(),
                            JsonNullable.undefined(),
                            JsonNullable.undefined()),
                    JsonNullable.of(EUR));

            when(userSettingsRepository.findByUserId(USER_ID)).thenReturn(Optional.of(userSettings));
            when(userSettingsRepository.save(userSettings)).thenReturn(userSettings);

            final UserSettings patchedUserSettings = userSettingsService.patchUserSettings(
                    USER_ID,
                    patchUserSettingsRequest);

            assertThat(patchedUserSettings.getCurrency()).isEqualTo(EUR);
            assertThat(patchedUserSettings.getTheme().background()).isEqualTo("#000000");
        }

        @Test
        void patchesNothingWhenCurrencyIsUndefined() {
            final UserSettings userSettings = createUser(USER_ID).getSettings();
            final PatchUserSettingsRequest patchUserSettingsRequest = new PatchUserSettingsRequest(
                    null,
                    JsonNullable.undefined());

            when(userSettingsRepository.findByUserId(USER_ID)).thenReturn(Optional.of(userSettings));
            when(userSettingsRepository.save(userSettings)).thenReturn(userSettings);

            final UserSettings patchedUserSettings = userSettingsService.patchUserSettings(
                    USER_ID,
                    patchUserSettingsRequest);

            assertThat(patchedUserSettings.getCurrency()).isEqualTo(UserSettings.DEFAULT_CURRENCY);
        }

        @Test
        void keepsCurrencyWhenOnlyThemeIsPatched() {
            final UserSettings userSettings = createUser(USER_ID).getSettings();
            userSettings.setCurrency(EUR);

            final PatchUserSettingsRequest patchUserSettingsRequest = new PatchUserSettingsRequest(
                    new ThemePatch(
                            JsonNullable.undefined(),
                            JsonNullable.undefined(),
                            JsonNullable.of("#000000"),
                            JsonNullable.undefined(),
                            JsonNullable.undefined()),
                    JsonNullable.undefined());

            when(userSettingsRepository.findByUserId(USER_ID)).thenReturn(Optional.of(userSettings));
            when(userSettingsRepository.save(userSettings)).thenReturn(userSettings);

            final UserSettings patchedUserSettings = userSettingsService.patchUserSettings(
                    USER_ID,
                    patchUserSettingsRequest);

            assertThat(patchedUserSettings.getCurrency()).isEqualTo(EUR);
            assertThat(patchedUserSettings.getTheme().text()).isEqualTo("#000000");
        }

        @Test
        void patchesNothingWhenThemeIsNull() {
            final UserSettings userSettings = createUser(USER_ID).getSettings();
            final PatchUserSettingsRequest patchUserSettingsRequest = new PatchUserSettingsRequest(null,
                    JsonNullable.undefined());

            when(userSettingsRepository.findByUserId(USER_ID)).thenReturn(Optional.of(userSettings));
            when(userSettingsRepository.save(userSettings)).thenReturn(userSettings);

            final UserSettings patchedUserSettings = userSettingsService.patchUserSettings(
                    USER_ID,
                    patchUserSettingsRequest);

            assertThat(patchedUserSettings.getTheme()).isEqualTo(Theme.DEFAULT);
            verify(userSettingsRepository).save(userSettings);
        }

        @Test
        void patchesNothingWhenAllColorsAreUndefined() {
            final UserSettings userSettings = createUser(USER_ID).getSettings();
            final PatchUserSettingsRequest patchUserSettingsRequest = new PatchUserSettingsRequest(
                    new ThemePatch(
                            JsonNullable.undefined(),
                            JsonNullable.undefined(),
                            JsonNullable.undefined(),
                            JsonNullable.undefined(),
                            JsonNullable.undefined()),
                    JsonNullable.undefined());

            when(userSettingsRepository.findByUserId(USER_ID)).thenReturn(Optional.of(userSettings));
            when(userSettingsRepository.save(userSettings)).thenReturn(userSettings);

            final UserSettings patchedUserSettings = userSettingsService.patchUserSettings(
                    USER_ID,
                    patchUserSettingsRequest);

            assertThat(patchedUserSettings.getTheme()).isEqualTo(Theme.DEFAULT);
        }

        @Test
        void throwsWhenUserSettingsNotFound() {
            when(userSettingsRepository.findByUserId(USER_ID)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> userSettingsService.patchUserSettings(
                    USER_ID,
                    new PatchUserSettingsRequest(null, JsonNullable.undefined())))
                    .isInstanceOf(NoSuchElementException.class);

            verify(userSettingsRepository, never()).save(any());
        }
    }
}
