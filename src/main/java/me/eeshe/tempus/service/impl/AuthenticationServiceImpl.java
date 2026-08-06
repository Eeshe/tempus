package me.eeshe.tempus.service.impl;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import me.eeshe.tempus.entity.User;
import me.eeshe.tempus.exception.UsernameAlreadyUsedException;
import me.eeshe.tempus.repository.UserRepository;
import me.eeshe.tempus.request.LoginRequest;
import me.eeshe.tempus.request.RegisterRequest;
import me.eeshe.tempus.service.AuthenticationService;

@Service
public class AuthenticationServiceImpl implements AuthenticationService {
    private final UserRepository userRepository;
    private final AuthenticationManager authenticationManager;
    private final PasswordEncoder passwordEncoder;

    public AuthenticationServiceImpl(
            UserRepository userRepository,
            AuthenticationManager authenticationManager,
            PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.authenticationManager = authenticationManager;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public User registerUser(RegisterRequest registerRequest) {
        userRepository.findByName(registerRequest.username()).ifPresent(user -> {
            throw new UsernameAlreadyUsedException(registerRequest.username());
        });
        return userRepository.save(new User(
                registerRequest.username(),
                passwordEncoder.encode(registerRequest.password())));
    }

    @Override
    public User loginUser(LoginRequest loginRequest) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        loginRequest.username(),
                        loginRequest.password()));

        final String username = loginRequest.username();
        return userRepository.findByName(username)
                .orElseThrow(() -> new UsernameNotFoundException(username));
    }

}
