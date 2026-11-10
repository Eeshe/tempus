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
    private static final Currency EUR = Currency.getInstance("EUR");

    @Nested
    class FindByUserId {

        @Test
        void returnsUserSettingsWithDefaultCurrency() {
            final User user = createUser("MyUser");

            final Optional<UserSettings> found = userSettingsRepository.findByUserId(user.getId());

            assertThat(found).isPresent();
            assertThat(found.get().getCurrency()).isEqualTo(UserSettings.DEFAULT_CURRENCY);
            assertThat(found.get().getTheme()).isEqualTo(Theme.DEFAULT);
        }

        @Test
        void returnsUserSettingsWithCustomCurrency() {
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
        void savePersistsDefaultCurrency() {
            final User user = createUser("MyUser");
            final long userSettingsId = user.getSettings().getId();

            entityManager.flush();
            entityManager.clear();

            final UserSettings saved = userSettingsRepository.findById(userSettingsId).orElseThrow();

            assertThat(saved.getCurrency()).isNotNull();
            assertThat(saved.getCurrency()).isEqualTo(UserSettings.DEFAULT_CURRENCY);
            assertThat(saved.getCurrency().getCurrencyCode()).isEqualTo("USD");
        }

        @Test
        void savePersistsCustomCurrency() {
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

        @Test
        void savePersistsCurrencyAlongsideTheme() {
            final User user = createUser("MyUser");
            final UserSettings userSettings = user.getSettings();
            userSettings.setCurrency(EUR);
            userSettings.setTheme(new Theme(
                    "#000000",
                    Theme.DEFAULT_SURFACE,
                    Theme.DEFAULT_TEXT,
                    Theme.DEFAULT_ACCENT,
                    Theme.DEFAULT_DANGER));

            final UserSettings saved = userSettingsRepository.save(userSettings);
            entityManager.flush();
            entityManager.clear();

            final UserSettings found = userSettingsRepository.findById(saved.getId()).orElseThrow();

            assertThat(found.getCurrency()).isEqualTo(EUR);
            assertThat(found.getTheme().background()).isEqualTo("#000000");
        }
    }
}
