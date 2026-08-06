package me.eeshe.tempus.service.impl;

import java.util.HashSet;
import java.util.List;

import org.springframework.stereotype.Service;

import me.eeshe.tempus.entity.User;
import me.eeshe.tempus.exception.UserNotFoundException;
import me.eeshe.tempus.repository.UserRepository;
import me.eeshe.tempus.request.CreateUserRequest;
import me.eeshe.tempus.request.PatchUserRequest;
import me.eeshe.tempus.request.UpdateUserRequest;
import me.eeshe.tempus.service.UserService;

@Service
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;

    public UserServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public List<User> listUsers() {
        return userRepository.findAll();
    }

    @Override
    public User getUser(long userId) {
        return userRepository.findById(userId).orElseThrow(() -> new UserNotFoundException(userId));
    }

    @Override
    public User createUser(CreateUserRequest createUserRequest) {
        return userRepository.save(new User(
                createUserRequest.name(),
                createUserRequest.password()));
    }

    @Override
    public User updateUser(long userId, UpdateUserRequest updateUserRequest) {
        final User user = getUser(userId);

        user.setName(updateUserRequest.name());
        user.setPassword(updateUserRequest.password());
        user.setGroups(new HashSet<>(updateUserRequest.groups()));

        return userRepository.save(user);
    }

    @Override
    public User patchUser(long userId, PatchUserRequest patchUserRequest) {
        final User user = getUser(userId);

        if (patchUserRequest.name() != null) {
            user.setName(patchUserRequest.name());
        }
        if (patchUserRequest.password() != null) {
            user.setPassword(patchUserRequest.password());
        }
        patchUserRequest.groups().ifPresent(user::setGroups);

        return userRepository.save(user);
    }

    @Override
    public void deleteUser(long userId) {
        userRepository.deleteById(userId);
    }
}
