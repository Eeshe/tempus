package me.eeshe.tempus.service;

import me.eeshe.tempus.entity.UserSettings;
import me.eeshe.tempus.request.PatchUserSettingsRequest;

public interface UserSettingsService {

    UserSettings patchUserSettings(long userId, PatchUserSettingsRequest patchUserSettingsRequest);
}
