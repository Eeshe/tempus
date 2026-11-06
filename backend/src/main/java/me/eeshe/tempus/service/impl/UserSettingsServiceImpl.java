package me.eeshe.tempus.service.impl;

import org.springframework.stereotype.Service;

import me.eeshe.tempus.entity.Theme;
import me.eeshe.tempus.entity.UserSettings;
import me.eeshe.tempus.repository.UserSettingsRepository;
import me.eeshe.tempus.request.PatchUserSettingsRequest;
import me.eeshe.tempus.request.ThemePatch;
import me.eeshe.tempus.service.UserSettingsService;

@Service
public class UserSettingsServiceImpl implements UserSettingsService {
    private final UserSettingsRepository userSettingsRepository;

    public UserSettingsServiceImpl(UserSettingsRepository userSettingsRepository) {
        this.userSettingsRepository = userSettingsRepository;
    }

    @Override
    public UserSettings patchUserSettings(long userId, PatchUserSettingsRequest patchUserSettingsRequest) {
        final UserSettings userSettings = userSettingsRepository.findByUserId(userId).orElseThrow();
        if (patchUserSettingsRequest.theme() != null) {
            final Theme currentTheme = userSettings.getTheme();
            final ThemePatch themePatch = patchUserSettingsRequest.theme();

            userSettings.setTheme(new Theme(
                    themePatch.background().orElse(currentTheme.background()),
                    themePatch.surface().orElse(currentTheme.surface()),
                    themePatch.text().orElse(currentTheme.text()),
                    themePatch.accent().orElse(currentTheme.accent()),
                    themePatch.danger().orElse(currentTheme.danger())));
        }
        return userSettingsRepository.save(userSettings);
    }
}
