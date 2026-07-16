package me.eeshe.tempus.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import me.eeshe.tempus.dto.CreateUserRequestDTO;
import me.eeshe.tempus.dto.UpdateUserRequestDTO;
import me.eeshe.tempus.dto.UserDTO;
import me.eeshe.tempus.entity.User;
import me.eeshe.tempus.mapper.UserMapper;
import me.eeshe.tempus.request.CreateUserRequest;
import me.eeshe.tempus.request.UpdateUserRequest;
import me.eeshe.tempus.service.UserService;

@RestController
@RequestMapping(path = "api/v1/users")
public class UserController {
    private final UserMapper userMapper;
    private final UserService userService;

    public UserController(UserMapper userMapper, UserService userService) {
        this.userMapper = userMapper;
        this.userService = userService;
    }

    // TODO: Switch to request body

    @GetMapping
    public ResponseEntity<List<UserDTO>> listUsers() {
        final List<User> users = userService.listUsers();
        final List<UserDTO> userDTOs = users.stream().map(userMapper::toDTO).toList();

        return ResponseEntity.ok(userDTOs);
    }

    @GetMapping(path = "/{userId}")
    public ResponseEntity<UserDTO> getUser(@PathVariable long userId) {
        final User user = userService.getUser(userId);
        final UserDTO userDTO = userMapper.toDTO(user);

        return ResponseEntity.ok(userDTO);
    }

    @PostMapping
    public ResponseEntity<UserDTO> createUser(@Valid CreateUserRequestDTO createUserRequestDTO) {
        final CreateUserRequest createUserRequest = userMapper.fromDTO(createUserRequestDTO);
        final User createdUser = userService.createUser(createUserRequest);
        final UserDTO createdUserDTO = userMapper.toDTO(createdUser);

        return new ResponseEntity<>(createdUserDTO, HttpStatus.CREATED);
    }

    @PutMapping(path = "/{userId}")
    public ResponseEntity<UserDTO> updateUser(
            @PathVariable long userId,
            @Valid UpdateUserRequestDTO updateUserRequestDTO) {
        final UpdateUserRequest updateUserRequest = userMapper.fromDTO(updateUserRequestDTO);
        final User updatedUser = userService.updateUser(userId, updateUserRequest);
        final UserDTO updatedUserDTO = userMapper.toDTO(updatedUser);

        return ResponseEntity.ok(updatedUserDTO);
    }

    @DeleteMapping(path = "/{userId}")
    public ResponseEntity<Void> deleteUser(@PathVariable long userId) {
        userService.deleteUser(userId);

        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}
