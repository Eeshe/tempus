package me.eeshe.tempus.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.openapitools.jackson.nullable.JsonNullable;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import me.eeshe.tempus.dto.PatchUserSettingsRequestDTO;
import me.eeshe.tempus.dto.ThemePatchDTO;
import me.eeshe.tempus.entity.UserSettings;
import me.eeshe.tempus.mapper.UserSettingsMapper;
import me.eeshe.tempus.request.PatchUserSettingsRequest;
import me.eeshe.tempus.request.ThemePatch;
import me.eeshe.tempus.service.UserSettingsService;
import me.eeshe.tempus.support.ControllerTestBase;

@WebMvcTest(UserSettingsController.class)
public class UserSettingsControllerTest extends ControllerTestBase {
    private static final String URL = "/api/v1/settings";

    @MockitoBean
    private UserSettingsService userSettingsService;

    @MockitoBean
    private UserSettingsMapper userSettingsMapper;

    @Nested
    class PatchUserSettings {

        @Test
        void patchesProvidedColors() {
            final UserSettings userSettings = createUser(USER_ID).getSettings();
            final PatchUserSettingsRequest patchUserSettingsRequest = new PatchUserSettingsRequest(
                    new ThemePatch(
                            JsonNullable.of("#000000"),
                            JsonNullable.undefined(),
                            JsonNullable.undefined(),
                            JsonNullable.undefined(),
                            JsonNullable.undefined()));

            when(userSettingsMapper.fromDTO(any(PatchUserSettingsRequestDTO.class)))
                    .thenReturn(patchUserSettingsRequest);
            when(userSettingsService.patchUserSettings(USER_ID, patchUserSettingsRequest))
                    .thenReturn(userSettings);
            when(userSettingsMapper.toDTO(userSettings)).thenReturn(createUserSettingsDTO());

            final String jsonBody = """
                    {
                        "theme": {
                            "background": "#000000"
                        }
                    }""";

            assertThat(mockMvc.patch().uri(URL).with(createPrincipalWithCsrf(USER_ID))
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(jsonBody))
                    .hasStatus(200)
                    .bodyJson()
                    .extractingPath("$.theme.background").asString().isEqualTo("#101318");

            verify(userSettingsMapper).fromDTO(any(PatchUserSettingsRequestDTO.class));
            verify(userSettingsService).patchUserSettings(USER_ID, patchUserSettingsRequest);
            verify(userSettingsMapper).toDTO(userSettings);
        }

        @Test
        void patchesNothingWhenThemeIsOmitted() {
            final UserSettings userSettings = createUser(USER_ID).getSettings();
            final PatchUserSettingsRequest patchUserSettingsRequest = new PatchUserSettingsRequest(null);

            when(userSettingsMapper.fromDTO(any(PatchUserSettingsRequestDTO.class)))
                    .thenReturn(patchUserSettingsRequest);
            when(userSettingsService.patchUserSettings(USER_ID, patchUserSettingsRequest))
                    .thenReturn(userSettings);
            when(userSettingsMapper.toDTO(userSettings)).thenReturn(createUserSettingsDTO());

            assertThat(mockMvc.patch().uri(URL).with(createPrincipalWithCsrf(USER_ID))
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("{}"))
                    .hasStatus(200)
                    .bodyJson()
                    .extractingPath("$.theme.background").asString().isEqualTo("#101318");

            verify(userSettingsService).patchUserSettings(USER_ID, patchUserSettingsRequest);
        }

        @Test
        void rejectsNullTheme() {
            assertThat(mockMvc.patch().uri(URL).with(createPrincipalWithCsrf(USER_ID))
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("""
                            {
                                "theme": null
                            }"""))
                    .hasStatus(400)
                    .bodyJson()
                    .extractingPath("$.error").asString()
                    .isEqualTo(PatchUserSettingsRequestDTO.ERROR_MESSAGE_NULL_THEME);

            verifyNoInteractions(userSettingsMapper, userSettingsService);
        }

        @Test
        void rejectsInvalidHexColor() {
            assertThat(mockMvc.patch().uri(URL).with(createPrincipalWithCsrf(USER_ID))
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("""
                            {
                                "theme": {
                                    "background": "red"
                                }
                            }"""))
                    .hasStatus(400)
                    .bodyJson()
                    .extractingPath("$.error").asString()
                    .isEqualTo(ThemePatchDTO.ERROR_MESSAGE_INVALID_BACKGROUND);

            verifyNoInteractions(userSettingsMapper, userSettingsService);
        }

        @Test
        void rejectsShortHexColor() {
            assertThat(mockMvc.patch().uri(URL).with(createPrincipalWithCsrf(USER_ID))
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("""
                            {
                                "theme": {
                                    "accent": "#FFF"
                                }
                            }"""))
                    .hasStatus(400)
                    .bodyJson()
                    .extractingPath("$.error").asString()
                    .isEqualTo(ThemePatchDTO.ERROR_MESSAGE_INVALID_ACCENT);

            verifyNoInteractions(userSettingsMapper, userSettingsService);
        }

        @Test
        void rejectsNullColor() {
            assertThat(mockMvc.patch().uri(URL).with(createPrincipalWithCsrf(USER_ID))
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("""
                            {
                                "theme": {
                                    "surface": null
                                }
                            }"""))
                    .hasStatus(400)
                    .bodyJson()
                    .extractingPath("$.error").asString()
                    .isEqualTo(ThemePatchDTO.ERROR_MESSAGE_NULL_SURFACE);

            verifyNoInteractions(userSettingsMapper, userSettingsService);
        }

        @Test
        void rejectsUnauthenticatedRequest() {
            assertThat(mockMvc.patch().uri(URL).with(createCsrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("{}"))
                    .hasStatus(401);

            verifyNoInteractions(userSettingsMapper, userSettingsService);
        }
    }
}
