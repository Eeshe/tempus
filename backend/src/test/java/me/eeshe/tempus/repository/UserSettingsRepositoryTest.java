package me.eeshe.tempus.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Currency;
import java.util.Optional;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import me.eeshe.tempus.entity.Theme;
import me.eeshe.tempus.entity.User;
import me.eeshe.tempus.entity.UserSettings;
import me.eeshe.tempus.support.RepositoryTestBase;

public class UserSettingsRepositoryTest extends RepositoryTestBase {
    private static final Theme CUSTOM_THEME = new Theme(
            "#000001",
            "#000002",
            "#000003",
            "#000004",
            "#000005");
    private static final Currency EUR = Currency.getInstance("EUR");

    @Nested
    class FindByUserId {

        @Test
        void returnsUserSettingsWithDefaultValues() {
            final User user = createUser("MyUser");

            final Optional<UserSettings> found = userSettingsRepository.findByUserId(user.getId());

            assertThat(found).isPresent();
            assertThat(found.get().getTheme()).isEqualTo(Theme.DEFAULT);
            assertThat(found.get().getCurrency()).isEqualTo(UserSettings.DEFAULT_CURRENCY);
        }

        @Test
        void returnsUserSettingsWithPatchedTheme() {
            final User user = createUser("MyUser");
            final UserSettings userSettings = user.getSettings();
            final Theme currentTheme = userSettings.getTheme();
            userSettings.setTheme(new Theme(
                    "#000000",
                    currentTheme.surface(),
                    currentTheme.text(),
                    currentTheme.accent(),
                    currentTheme.danger()));

            entityManager.flush();
            entityManager.clear();

            final Optional<UserSettings> found = userSettingsRepository.findByUserId(user.getId());

            assertThat(found).isPresent();
            assertThat(found.get().getTheme()).isEqualTo(new Theme(
                    "#000000",
                    Theme.DEFAULT_SURFACE,
                    Theme.DEFAULT_TEXT,
                    Theme.DEFAULT_ACCENT,
                    Theme.DEFAULT_DANGER));
            assertThat(found.get().getCurrency()).isEqualTo(UserSettings.DEFAULT_CURRENCY);
        }

        @Test
        void returnsUserSettingsWithPatchedCurrency() {
            final User user = createUser("MyUser");
            final UserSettings userSettings = user.getSettings();
            userSettings.setCurrency(EUR);

            entityManager.flush();
            entityManager.clear();

            final Optional<UserSettings> found = userSettingsRepository.findByUserId(user.getId());

            assertThat(found).isPresent();
            assertThat(found.get().getCurrency()).isEqualTo(EUR);
        }

        @Test
        void returnsEmptyWhenUserHasNoSettings() {
            assertThat(userSettingsRepository.findByUserId(999L)).isEmpty();
        }
    }

    @Nested
    class SaveUserSettings {

        @Test
        void savePersistsDefaultValues() {
            final User user = createUser("MyUser");
            final long userSettingsId = user.getSettings().getId();

            entityManager.flush();
            entityManager.clear();

            final UserSettings saved = userSettingsRepository.findById(userSettingsId).orElseThrow();

            assertThat(saved.getCurrency()).isNotNull();
            assertThat(saved.getTheme()).isEqualTo(Theme.DEFAULT);
            assertThat(saved.getCurrency()).isEqualTo(UserSettings.DEFAULT_CURRENCY);
            assertThat(saved.getCurrency().getCurrencyCode()).isEqualTo("USD");
        }

        @Test
        void savePersistsDefaultThemeToAllColumns() {
            final User user = createUser("MyUser");
            final long userSettingsId = user.getSettings().getId();

            entityManager.flush();
            entityManager.clear();

            final UserSettings found = userSettingsRepository.findById(userSettingsId).orElseThrow();

            assertThat(found.getTheme()).isEqualTo(Theme.DEFAULT);
            assertThat(findThemeColumns(userSettingsId)).containsExactly(
                    Theme.DEFAULT_BACKGROUND,
                    Theme.DEFAULT_SURFACE,
                    Theme.DEFAULT_TEXT,
                    Theme.DEFAULT_ACCENT,
                    Theme.DEFAULT_DANGER);
        }

        @Test
        void savePersistsAllThemeComponentsToTheirColumns() {
            final User user = createUser("MyUser");
            final UserSettings userSettings = user.getSettings();
            userSettings.setTheme(CUSTOM_THEME);

            final UserSettings saved = userSettingsRepository.save(userSettings);
            entityManager.flush();
            entityManager.clear();

            final UserSettings found = userSettingsRepository.findById(saved.getId()).orElseThrow();

            assertThat(found.getTheme()).isEqualTo(CUSTOM_THEME);
            assertThat(findThemeColumns(saved.getId())).containsExactly(
                    "#000001",
                    "#000002",
                    "#000003",
                    "#000004",
                    "#000005");
        }

        @Test
        void savePersistsPartiallyPatchedTheme() {
            final User user = createUser("MyUser");
            final UserSettings userSettings = user.getSettings();
            final Theme currentTheme = userSettings.getTheme();
            userSettings.setTheme(new Theme(
                    "#000000",
                    currentTheme.surface(),
                    currentTheme.text(),
                    currentTheme.accent(),
                    currentTheme.danger()));

            final UserSettings saved = userSettingsRepository.save(userSettings);
            entityManager.flush();
            entityManager.clear();

            final UserSettings found = userSettingsRepository.findById(saved.getId()).orElseThrow();

            assertThat(found.getTheme()).isEqualTo(new Theme(
                    "#000000",
                    Theme.DEFAULT_SURFACE,
                    Theme.DEFAULT_TEXT,
                    Theme.DEFAULT_ACCENT,
                    Theme.DEFAULT_DANGER));
            assertThat(findThemeColumns(saved.getId())).containsExactly(
                    "#000000",
                    Theme.DEFAULT_SURFACE,
                    Theme.DEFAULT_TEXT,
                    Theme.DEFAULT_ACCENT,
                    Theme.DEFAULT_DANGER);
        }

        @Test
        void saveKeepsCurrencyWhenThemeIsPatched() {
            final User user = createUser("MyUser");
            final UserSettings userSettings = user.getSettings();
            userSettings.setCurrency(EUR);
            userSettings.setTheme(CUSTOM_THEME);

            final UserSettings saved = userSettingsRepository.save(userSettings);
            entityManager.flush();
            entityManager.clear();

            final UserSettings found = userSettingsRepository.findById(saved.getId()).orElseThrow();

            assertThat(found.getCurrency()).isEqualTo(EUR);
            assertThat(found.getTheme()).isEqualTo(CUSTOM_THEME);
            assertThat(findThemeColumns(saved.getId())).containsExactly(
                    "#000001",
                    "#000002",
                    "#000003",
                    "#000004",
                    "#000005");
        }

        @Test
        void savePersistsPatchedCurrency() {
            final User user = createUser("MyUser");
            final UserSettings userSettings = user.getSettings();
            userSettings.setCurrency(EUR);

            final UserSettings saved = userSettingsRepository.save(userSettings);
            entityManager.flush();
            entityManager.clear();

            final UserSettings found = userSettingsRepository.findById(saved.getId()).orElseThrow();

            assertThat(found.getCurrency()).isEqualTo(EUR);
            assertThat(found.getCurrency().getCurrencyCode()).isEqualTo("EUR");
        }
    }

    /**
     * Reads the physical theme columns of a {@link UserSettings} row, so a
     * mistyped or swapped {@code @AttributeOverride} can't slip through an
     * entity round-trip unnoticed.
     */
    private Object[] findThemeColumns(long userSettingsId) {
        return (Object[]) entityManager.getEntityManager()
                .createNativeQuery("""
                        SELECT background_color, surface_color, text_color, accent_color, danger_color
                        FROM user_settings
                        WHERE id = ?1""")
                .setParameter(1, userSettingsId)
                .getSingleResult();
    }
}
