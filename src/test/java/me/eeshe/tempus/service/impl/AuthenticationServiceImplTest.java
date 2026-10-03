package me.eeshe.tempus.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import me.eeshe.tempus.entity.User;
import me.eeshe.tempus.exception.UsernameAlreadyUsedException;
import me.eeshe.tempus.repository.UserRepository;
import me.eeshe.tempus.request.LoginRequest;
import me.eeshe.tempus.request.RegisterRequest;
import me.eeshe.tempus.support.EntityTestBase;

@ExtendWith(MockitoExtension.class)
public class AuthenticationServiceImplTest extends EntityTestBase {
    private static final String USER_NAME = "MyUser";
    private static final String PASSWORD = "MyPassword";
    private static final String ENCODED_PASSWORD = "MyEncodedPassword";

    @Mock
    private UserRepository userRepository;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @InjectMocks
    private AuthenticationServiceImpl authenticationService;

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @Nested
    class RegisterUser {

        @Test
        void savesUserWithEncodedPassword() {
            final User savedUser = createUser(USER_ID);

            when(userRepository.findByName(USER_NAME)).thenReturn(Optional.empty());
            when(passwordEncoder.encode(PASSWORD)).thenReturn(ENCODED_PASSWORD);
            when(userRepository.save(any(User.class))).thenReturn(savedUser);

            final User user = authenticationService.registerUser(new RegisterRequest(USER_NAME, PASSWORD));

            final ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);

            verify(userRepository).save(userCaptor.capture());

            assertThat(userCaptor.getValue().getName()).isEqualTo(USER_NAME);
            assertThat(userCaptor.getValue().getPassword()).isEqualTo(ENCODED_PASSWORD);
            assertThat(user).isEqualTo(savedUser);
        }

        @Test
        void throwsWhenUsernameAlreadyUsed() {
            when(userRepository.findByName(USER_NAME)).thenReturn(Optional.of(createUser(USER_ID)));

            assertThatThrownBy(() -> authenticationService.registerUser(new RegisterRequest(USER_NAME, PASSWORD)))
                    .isInstanceOf(UsernameAlreadyUsedException.class)
                    .hasMessage("Username MyUser is already used by another user");

            verify(userRepository, never()).save(any());
            verifyNoInteractions(passwordEncoder);
        }
    }

    @Nested
    class LoginUser {

        @Test
        void authenticatesAndReturnsUser() {
            final User user = createUser(USER_ID);

            when(authenticationManager.authenticate(any())).thenReturn(mock(Authentication.class));
            when(userRepository.findByName(USER_NAME)).thenReturn(Optional.of(user));

            final User loggedInUser = authenticationService.loginUser(
                    new LoginRequest(USER_NAME, PASSWORD), request, response);

            assertThat(loggedInUser).isEqualTo(user);
        }

        @Test
        void throwsWhenUserNotFoundAfterAuthentication() {
            when(authenticationManager.authenticate(any())).thenReturn(mock(Authentication.class));
            when(userRepository.findByName(USER_NAME)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> authenticationService.loginUser(
                    new LoginRequest(USER_NAME, PASSWORD), request, response))
                    .isInstanceOf(UsernameNotFoundException.class)
                    .hasMessage("MyUser");
        }
    }
}
