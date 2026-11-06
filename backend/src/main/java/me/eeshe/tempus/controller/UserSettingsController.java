package me.eeshe.tempus.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import me.eeshe.tempus.dto.PatchUserSettingsRequestDTO;
import me.eeshe.tempus.dto.UserSettingsDTO;
import me.eeshe.tempus.entity.UserSettings;
import me.eeshe.tempus.mapper.UserSettingsMapper;
import me.eeshe.tempus.request.PatchUserSettingsRequest;
import me.eeshe.tempus.security.UserDetailsImpl;
import me.eeshe.tempus.service.UserSettingsService;

@RestController
@RequestMapping(path = "api/v1/settings")
public class UserSettingsController {
    private final UserSettingsService userSettingsService;
    private final UserSettingsMapper userSettingsMapper;

    public UserSettingsController(
            UserSettingsService userSettingsService,
            UserSettingsMapper userSettingsMapper) {
        this.userSettingsService = userSettingsService;
        this.userSettingsMapper = userSettingsMapper;
    }

    @PatchMapping
    public ResponseEntity<UserSettingsDTO> patchUserSettings(
            @Valid @RequestBody PatchUserSettingsRequestDTO patchUserSettingsRequestDTO,
            @AuthenticationPrincipal UserDetailsImpl userDetails) {
        final PatchUserSettingsRequest patchUserSettingsRequest = userSettingsMapper
                .fromDTO(patchUserSettingsRequestDTO);
        final UserSettings userSettings = userSettingsService.patchUserSettings(
                userDetails.getId(),
                patchUserSettingsRequest);
        final UserSettingsDTO userSettingsDTO = userSettingsMapper.toDTO(userSettings);

        return ResponseEntity.ok(userSettingsDTO);
    }
}
