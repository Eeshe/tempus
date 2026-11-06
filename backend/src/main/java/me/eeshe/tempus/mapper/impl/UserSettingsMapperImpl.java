package me.eeshe.tempus.mapper.impl;

import org.springframework.stereotype.Component;

import me.eeshe.tempus.dto.PatchUserSettingsRequestDTO;
import me.eeshe.tempus.dto.ThemeDTO;
import me.eeshe.tempus.dto.ThemePatchDTO;
import me.eeshe.tempus.dto.UserSettingsDTO;
import me.eeshe.tempus.entity.Theme;
import me.eeshe.tempus.entity.UserSettings;
import me.eeshe.tempus.mapper.UserSettingsMapper;
import me.eeshe.tempus.request.PatchUserSettingsRequest;
import me.eeshe.tempus.request.ThemePatch;

@Component
public class UserSettingsMapperImpl implements UserSettingsMapper {

    @Override
    public UserSettingsDTO toDTO(UserSettings userSettings) {
        final Theme theme = userSettings.getTheme();

        return new UserSettingsDTO(new ThemeDTO(
                theme.background(),
                theme.surface(),
                theme.text(),
                theme.accent(),
                theme.danger()));
    }

    @Override
    public PatchUserSettingsRequest fromDTO(PatchUserSettingsRequestDTO patchUserSettingsRequestDTO) {
        final ThemePatchDTO theme = patchUserSettingsRequestDTO.theme().orElse(null);
        if (theme == null) {
            return new PatchUserSettingsRequest(null);
        }
        return new PatchUserSettingsRequest(new ThemePatch(
                theme.background(),
                theme.surface(),
                theme.text(),
                theme.accent(),
                theme.danger()));
    }
}
