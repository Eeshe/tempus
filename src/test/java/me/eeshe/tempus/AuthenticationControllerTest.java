package me.eeshe.tempus;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.time.Instant;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import me.eeshe.tempus.controller.AuthenticationController;
import me.eeshe.tempus.dto.LoginRequestDTO;
import me.eeshe.tempus.dto.RegisterRequestDTO;
import me.eeshe.tempus.entity.User;
import me.eeshe.tempus.exception.UsernameAlreadyUsedException;
import me.eeshe.tempus.mapper.AuthenticationMapper;
import me.eeshe.tempus.mapper.UserMapper;
import me.eeshe.tempus.request.LoginRequest;
import me.eeshe.tempus.request.RegisterRequest;
import me.eeshe.tempus.service.AuthenticationService;
import me.eeshe.tempus.service.UserService;
import me.eeshe.tempus.support.ControllerTestBase;

@WebMvcTest(AuthenticationController.class)
public class AuthenticationControllerTest extends ControllerTestBase {
    private static final long USER_ID = 1L;
    private static final Instant CREATED_AT = Instant.parse("2026-01-01T10:00:00Z");

    @MockitoBean
    private AuthenticationService authenticationService;

    @MockitoBean
    private AuthenticationMapper authenticationMapper;

    @MockitoBean
    private UserService userService;

    @MockitoBean
    private UserMapper userMapper;

    @Nested
    class CheckAuthenticated {
        private static final String URL = "/api/v1/auth/me";

        @Test
        void returnsAuthenticatedUser() {
            when(userService.getUser(USER_ID)).thenReturn(createUser(USER_ID));
            when(userMapper.toDTO(any(User.class))).thenReturn(createTestUserDTO(USER_ID, CREATED_AT));

            assertThat(mockMvc.get().uri(URL).with(createPrincipal(USER_ID)))
                    .hasStatusOk()
                    .bodyJson()
                    .extractingPath("$.id").asNumber().isEqualTo(1);

            verify(userService).getUser(USER_ID);
        }

        @Test
        void rejectsUnauthenticatedRequest() {
            assertThat(mockMvc.get().uri(URL)).hasStatus(401);

            verifyNoInteractions(userService);
        }
    }

    @Nested
    class Register {
        private static final String URL = "/api/v1/auth/register";

        @Test
        void returnsRegisteredUser() {
            final RegisterRequest registerRequest = createRegisterRequest();
            final RegisterRequestDTO registerRequestDTO = createRegisterRequestDTO();

            when(authenticationMapper.fromDTO(eq(registerRequestDTO))).thenReturn(registerRequest);
            when(authenticationService.registerUser(registerRequest)).thenReturn(createUser(USER_ID));
            when(userMapper.toDTO(any(User.class))).thenReturn(createTestUserDTO(USER_ID, CREATED_AT));

            final String jsonBody = """
                    {
                        "username": "MyUser",
                        "password": "MyPassword"
                    }""";

            assertThat(mockMvc.post().uri(URL)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(jsonBody))
                    .hasStatus(201)
                    .bodyJson()
                    .extractingPath("$.name").asString().isEqualTo("MyUser");

            verify(authenticationMapper).fromDTO(registerRequestDTO);
            verify(authenticationService).registerUser(registerRequest);
        }

        @Test
        void rejectsBlankCredentials() {
            final String jsonBody = """
                    {
                        "username": "",
                        "password": ""
                    }""";
            assertThat(mockMvc.post().uri(URL)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(jsonBody))
                    .hasStatus(400);

            verifyNoInteractions(authenticationMapper, authenticationService);
        }

        @Test
        void rejectsNullCredentials() {
            final String jsonBody = """
                    {
                        "username": null,
                        "password": null
                    }""";
            assertThat(mockMvc.post().uri(URL)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(jsonBody))
                    .hasStatus(400);

            verifyNoInteractions(authenticationMapper, authenticationService);
        }

        @Test
        void rejectsNonProvidedCredentials() {
            final String jsonBody = "";
            assertThat(mockMvc.post().uri(URL)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(jsonBody))
                    .hasStatus(400);

            verifyNoInteractions(authenticationMapper, authenticationService);
        }

        @Test
        void rejectsUsedUsername() {
            final RegisterRequest registerRequest = createRegisterRequest();
            final RegisterRequestDTO registerRequestDTO = createRegisterRequestDTO();

            when(authenticationMapper.fromDTO(eq(registerRequestDTO))).thenReturn(registerRequest);
            when(authenticationService.registerUser(registerRequest))
                    .thenThrow(new UsernameAlreadyUsedException("MyUser"));

            final String jsonBody = """
                    {
                        "username": "MyUser",
                        "password": "MyPassword"
                    }""";

            assertThat(mockMvc.post().uri(URL)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(jsonBody))
                    .hasStatus(400)
                    .bodyJson()
                    .extractingPath("$.error").asString()
                    .isEqualTo(new UsernameAlreadyUsedException("MyUser").getMessage());

            verify(authenticationMapper).fromDTO(registerRequestDTO);
            verify(authenticationService).registerUser(registerRequest);
            verifyNoInteractions(userMapper);
        }
    }

    @Nested
    class Login {
        private static final String URL = "/api/v1/auth/login";

        @Test
        void returnsLoggedInUserWithoutCredentials() {
            final LoginRequest loginRequest = createLoginRequest();
            final LoginRequestDTO loginRequestDTO = createLoginRequestDTO();

            when(authenticationMapper.fromDTO(eq(loginRequestDTO))).thenReturn(loginRequest);
            when(authenticationService.loginUser(
                    eq(loginRequest),
                    any(HttpServletRequest.class),
                    any(HttpServletResponse.class)))
                    .thenReturn(createUser(USER_ID));
            when(userMapper.toDTO(any(User.class))).thenReturn(createTestUserDTO(USER_ID, CREATED_AT));

            final String jsonBody = """
                    {
                        "username": "MyUser",
                        "password": "MyPassword"
                    }""";

            assertThat(mockMvc.post().uri(URL)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(jsonBody))
                    .hasStatus(200)
                    .bodyJson()
                    .extractingPath("$.name").asString().isEqualTo("MyUser");

            verify(authenticationMapper).fromDTO(loginRequestDTO);
            verify(authenticationService).loginUser(
                    eq(loginRequest),
                    any(HttpServletRequest.class),
                    any(HttpServletResponse.class));
        }

        @Test
        void rejectsBadCredentials() {
            final LoginRequest loginRequest = createLoginRequest();
            final LoginRequestDTO loginRequestDTO = createLoginRequestDTO();

            when(authenticationMapper.fromDTO(eq(loginRequestDTO))).thenReturn(loginRequest);
            when(authenticationService.loginUser(
                    eq(loginRequest),
                    any(HttpServletRequest.class),
                    any(HttpServletResponse.class))).thenThrow(new BadCredentialsException("Bad credentials"));

            final String jsonBody = """
                    {
                        "username": "MyUser",
                        "password": "MyPassword"
                    }""";

            assertThat(mockMvc.post().uri(URL)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(jsonBody))
                    .hasStatus(401)
                    .bodyJson()
                    .extractingPath("$.message").asString().isEqualTo("Unauthorized");

            verify(authenticationMapper).fromDTO(loginRequestDTO);
            verify(authenticationService).loginUser(
                    eq(loginRequest),
                    any(HttpServletRequest.class),
                    any(HttpServletResponse.class));
            verifyNoInteractions(userMapper);
        }

        @Test
        void rejectsBlankCredentials() {
            final String jsonBody = """
                    {
                        "username": "",
                        "password": ""
                    }""";
            assertThat(mockMvc.post().uri(URL)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(jsonBody))
                    .hasStatus(400);

            verifyNoInteractions(authenticationMapper, authenticationService);
        }

        @Test
        void rejectsNullCredentials() {
            final String jsonBody = """
                    {
                        "username": null,
                        "password": null
                    }""";
            assertThat(mockMvc.post().uri(URL)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(jsonBody))
                    .hasStatus(400);

            verifyNoInteractions(authenticationMapper, authenticationService);
        }

        @Test
        void rejectsNonProvidedCredentials() {
            final String jsonBody = "";
            assertThat(mockMvc.post().uri(URL)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(jsonBody))
                    .hasStatus(400);

            verifyNoInteractions(authenticationMapper, authenticationService);
        }
    }

    @Nested
    class Logout {
        private static final String URL = "/api/v1/auth/logout";

        @Test
        void logsOutAuthenticatedUser() {
            assertThat(mockMvc.post().uri(URL).with(createPrincipal(USER_ID))).hasStatus(204);

            verify(authenticationService).logoutUser(
                    any(HttpServletRequest.class),
                    any(HttpServletResponse.class));
        }

        @Test
        void rejectsUnauthenticatedUserLogout() {
            assertThat(mockMvc.post().uri(URL)).hasStatus(401);

            verifyNoInteractions(authenticationService);
        }
    }

    private static RegisterRequest createRegisterRequest() {
        return new RegisterRequest("MyUser", "MyPassword");
    }

    private static RegisterRequestDTO createRegisterRequestDTO() {
        return new RegisterRequestDTO("MyUser", "MyPassword");
    }

    private static LoginRequest createLoginRequest() {
        return new LoginRequest("MyUser", "MyPassword");
    }

    private static LoginRequestDTO createLoginRequestDTO() {
        return new LoginRequestDTO("MyUser", "MyPassword");
    }
}
