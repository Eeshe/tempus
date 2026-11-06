package me.eeshe.tempus.mapper;

import me.eeshe.tempus.dto.PatchUserSettingsRequestDTO;
import me.eeshe.tempus.dto.UserSettingsDTO;
import me.eeshe.tempus.entity.UserSettings;
import me.eeshe.tempus.request.PatchUserSettingsRequest;

public interface UserSettingsMapper {

    UserSettingsDTO toDTO(UserSettings userSettings);

    PatchUserSettingsRequest fromDTO(PatchUserSettingsRequestDTO patchUserSettingsRequestDTO);
}
