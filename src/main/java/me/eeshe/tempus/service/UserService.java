package me.eeshe.tempus.service;

import java.util.List;

import me.eeshe.tempus.entity.User;
import me.eeshe.tempus.request.CreateUserRequest;
import me.eeshe.tempus.request.PatchUserRequest;
import me.eeshe.tempus.request.UpdateUserRequest;

public interface UserService {

    List<User> listUsers();

    User getUser(long userId);

    User createUser(CreateUserRequest createUserRequest);

    User updateUser(long userId, UpdateUserRequest updateUserRequest);

    User patchUser(long userId, PatchUserRequest patchUserRequest);

    void deleteUser(long userId);
}
