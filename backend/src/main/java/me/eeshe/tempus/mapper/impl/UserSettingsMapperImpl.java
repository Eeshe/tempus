package me.eeshe.tempus.mapper.impl;

import java.util.Currency;

import org.openapitools.jackson.nullable.JsonNullable;
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
                theme.danger()),
                userSettings.getCurrency());
    }

    @Override
    public PatchUserSettingsRequest fromDTO(PatchUserSettingsRequestDTO patchUserSettingsRequestDTO) {
        final ThemePatchDTO theme = patchUserSettingsRequestDTO.theme().orElse(null);
        final JsonNullable<Currency> currency = patchUserSettingsRequestDTO.currency();
        if (theme == null) {
            return new PatchUserSettingsRequest(null, currency);
        }
        return new PatchUserSettingsRequest(new ThemePatch(
                theme.background(),
                theme.surface(),
                theme.text(),
                theme.accent(),
                theme.danger()),
                currency);
    }
}
