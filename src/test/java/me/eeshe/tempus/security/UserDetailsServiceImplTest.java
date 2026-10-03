package me.eeshe.tempus.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import me.eeshe.tempus.repository.UserRepository;
import me.eeshe.tempus.support.EntityTestBase;

@ExtendWith(MockitoExtension.class)
public class UserDetailsServiceImplTest extends EntityTestBase {
    private static final String USER_NAME = "MyUser";

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserDetailsServiceImpl userDetailsService;

    @Nested
    class LoadUserByUsername {

        @Test
        void returnsUserDetailsWhenUserFound() {
            when(userRepository.findByName(USER_NAME)).thenReturn(Optional.of(createUser(USER_ID)));

            final UserDetailsImpl userDetails = (UserDetailsImpl) userDetailsService.loadUserByUsername(USER_NAME);

            assertThat(userDetails.getId()).isEqualTo(USER_ID);
            assertThat(userDetails.getUsername()).isEqualTo(USER_NAME);
            assertThat(userDetails.getPassword()).isEqualTo("MyPassword");
        }

        @Test
        void throwsWhenUserNotFound() {
            when(userRepository.findByName(USER_NAME)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> userDetailsService.loadUserByUsername(USER_NAME))
                    .isInstanceOf(UsernameNotFoundException.class)
                    .hasMessage("MyUser");
        }
    }
}
