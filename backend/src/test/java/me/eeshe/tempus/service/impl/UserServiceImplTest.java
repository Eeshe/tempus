package me.eeshe.tempus.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import me.eeshe.tempus.entity.User;
import me.eeshe.tempus.exception.UserNotFoundException;
import me.eeshe.tempus.repository.UserRepository;
import me.eeshe.tempus.request.CreateUserRequest;
import me.eeshe.tempus.request.PatchUserRequest;
import me.eeshe.tempus.request.UpdateUserRequest;
import me.eeshe.tempus.support.EntityTestBase;

@ExtendWith(MockitoExtension.class)
public class UserServiceImplTest extends EntityTestBase {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserServiceImpl userService;

    @Nested
    class ListUsers {

        @Test
        void returnsAllUsers() {
            final User firstUser = createUser(USER_ID);
            final User secondUser = createUser(2L);

            when(userRepository.findAll()).thenReturn(List.of(firstUser, secondUser));

            final List<User> users = userService.listUsers();

            assertThat(users).containsExactly(firstUser, secondUser);
            verify(userRepository).findAll();
        }
    }

    @Nested
    class GetUser {

        @Test
        void returnsUserWhenFound() {
            final User user = createUser(USER_ID);
            when(userRepository.findById(USER_ID)).thenReturn(Optional.of(user));

            assertThat(userService.getUser(USER_ID)).isEqualTo(user);
        }

        @Test
        void throwsWhenUserNotFound() {
            when(userRepository.findById(USER_ID)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> userService.getUser(USER_ID))
                    .isInstanceOf(UserNotFoundException.class)
                    .hasMessage("User with ID 1 does not exist");
        }
    }

    @Nested
    class CreateUser {

        @Test
        void savesUserWithRequestFields() {
            final CreateUserRequest createUserRequest = new CreateUserRequest("MyNewUser", "MyNewPassword");
            final User savedUser = createUser(USER_ID);
            when(userRepository.save(any(User.class))).thenReturn(savedUser);

            final User user = userService.createUser(createUserRequest);

            final ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);

            verify(userRepository).save(userCaptor.capture());

            assertThat(userCaptor.getValue().getName()).isEqualTo("MyNewUser");
            assertThat(userCaptor.getValue().getPassword()).isEqualTo("MyNewPassword");
            assertThat(user).isEqualTo(savedUser);
        }
    }

    @Nested
    class UpdateUser {

        @Test
        void updatesNameAndPasswordAndSaves() {
            final User user = createUser(USER_ID);

            when(userRepository.findById(USER_ID)).thenReturn(Optional.of(user));
            when(userRepository.save(user)).thenReturn(user);

            final User updatedUser = userService.updateUser(
                    USER_ID, new UpdateUserRequest("MyNewUser", "MyNewPassword"));

            assertThat(updatedUser.getName()).isEqualTo("MyNewUser");
            assertThat(updatedUser.getPassword()).isEqualTo("MyNewPassword");

            verify(userRepository).save(user);
        }

        @Test
        void throwsWhenUserNotFound() {
            when(userRepository.findById(USER_ID)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> userService.updateUser(
                    USER_ID, new UpdateUserRequest("MyNewUser", "MyNewPassword")))
                    .isInstanceOf(UserNotFoundException.class)
                    .hasMessage("User with ID 1 does not exist");

            verify(userRepository, never()).save(any());
        }
    }

    @Nested
    class PatchUser {

        @Test
        void patchesOnlyProvidedFields() {
            final User user = createUser(USER_ID);

            when(userRepository.findById(USER_ID)).thenReturn(Optional.of(user));
            when(userRepository.save(user)).thenReturn(user);

            final User patchedUser = userService.patchUser(USER_ID, new PatchUserRequest("MyNewUser", null));

            assertThat(patchedUser.getName()).isEqualTo("MyNewUser");
            assertThat(patchedUser.getPassword()).isEqualTo("MyPassword");

            verify(userRepository).save(user);
        }

        @Test
        void patchesOnlyProvidedPassword() {
            final User user = createUser(USER_ID);

            when(userRepository.findById(USER_ID)).thenReturn(Optional.of(user));
            when(userRepository.save(user)).thenReturn(user);

            final User patchedUser = userService.patchUser(USER_ID, new PatchUserRequest(null, "MyNewPassword"));

            assertThat(patchedUser.getName()).isEqualTo("MyUser");
            assertThat(patchedUser.getPassword()).isEqualTo("MyNewPassword");

            verify(userRepository).save(user);
        }

        @Test
        void patchesNothingWhenAllFieldsNull() {
            final User user = createUser(USER_ID);

            when(userRepository.findById(USER_ID)).thenReturn(Optional.of(user));
            when(userRepository.save(user)).thenReturn(user);

            final User patchedUser = userService.patchUser(USER_ID, new PatchUserRequest(null, null));

            assertThat(patchedUser.getName()).isEqualTo("MyUser");
            assertThat(patchedUser.getPassword()).isEqualTo("MyPassword");

            verify(userRepository).save(user);
        }

        @Test
        void throwsWhenUserNotFound() {
            when(userRepository.findById(USER_ID)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> userService.patchUser(USER_ID, new PatchUserRequest("MyNewUser", null)))
                    .isInstanceOf(UserNotFoundException.class)
                    .hasMessage("User with ID 1 does not exist");

            verify(userRepository, never()).save(any());
        }
    }

    @Nested
    class DeleteUser {

        @Test
        void deletesUser() {
            userService.deleteUser(USER_ID);

            verify(userRepository).deleteById(USER_ID);
        }
    }
}
