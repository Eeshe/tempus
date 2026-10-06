package me.eeshe.tempus.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import me.eeshe.tempus.dto.LoginRequestDTO;
import me.eeshe.tempus.dto.RegisterRequestDTO;
import me.eeshe.tempus.dto.UserDTO;
import me.eeshe.tempus.entity.User;
import me.eeshe.tempus.mapper.AuthenticationMapper;
import me.eeshe.tempus.mapper.UserMapper;
import me.eeshe.tempus.request.LoginRequest;
import me.eeshe.tempus.request.RegisterRequest;
import me.eeshe.tempus.security.UserDetailsImpl;
import me.eeshe.tempus.service.AuthenticationService;
import me.eeshe.tempus.service.UserService;

@RestController
@RequestMapping(path = "api/v1/auth")
public class AuthenticationController {
    private final AuthenticationService authenticationService;
    private final AuthenticationMapper authenticationMapper;
    private final UserService userService;
    private final UserMapper userMapper;

    public AuthenticationController(
            AuthenticationService authenticationService,
            AuthenticationMapper authenticationMapper,
            UserService userService,
            UserMapper userMapper) {
        this.authenticationService = authenticationService;
        this.authenticationMapper = authenticationMapper;
        this.userService = userService;
        this.userMapper = userMapper;
    }

    @GetMapping(path = "/me")
    public ResponseEntity<UserDTO> checkAuthenticated(
            @AuthenticationPrincipal UserDetailsImpl userDetails) {
        final User user = userService.getUser(userDetails.getId());
        final UserDTO userDTO = userMapper.toDTO(user);

        return ResponseEntity.ok(userDTO);
    }

    @PostMapping(path = "/register")
    public ResponseEntity<UserDTO> register(@Valid @RequestBody RegisterRequestDTO registerRequestDTO) {
        final RegisterRequest registerRequest = authenticationMapper.fromDTO(registerRequestDTO);
        final User registeredUser = authenticationService.registerUser(registerRequest);
        final UserDTO registeredUserDTO = userMapper.toDTO(registeredUser);

        return new ResponseEntity<>(registeredUserDTO, HttpStatus.CREATED);
    }

    @PostMapping(path = "/login")
    public ResponseEntity<UserDTO> login(
            @Valid @RequestBody LoginRequestDTO loginRequestDTO,
            HttpServletRequest request,
            HttpServletResponse response) {
        final LoginRequest loginRequest = authenticationMapper.fromDTO(loginRequestDTO);
        final User loggedUser = authenticationService.loginUser(
                loginRequest,
                request,
                response);
        final UserDTO loggedUserDTO = userMapper.toDTO(loggedUser);

        return ResponseEntity.ok(loggedUserDTO);
    }

    @PostMapping(path = "/logout")
    public ResponseEntity<Void> logout(
            HttpServletRequest request,
            HttpServletResponse response) {
        authenticationService.logoutUser(request, response);

        return ResponseEntity.noContent().build();
    }
}
