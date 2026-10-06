package me.eeshe.tempus.service.impl;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.stereotype.Service;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
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
    public User loginUser(LoginRequest loginRequest, HttpServletRequest request,
            HttpServletResponse response) {
        final String username = loginRequest.username();
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        username,
                        loginRequest.password()));

        SecurityContext securityContext = SecurityContextHolder.createEmptyContext();
        securityContext.setAuthentication(authentication);
        SecurityContextHolder.setContext(securityContext);

        new HttpSessionSecurityContextRepository().saveContext(
                securityContext,
                request,
                response);

        return userRepository.findByName(username)
                .orElseThrow(() -> new UsernameNotFoundException(username));
    }

    @Override
    public void logoutUser(HttpServletRequest request, HttpServletResponse response) {
        new SecurityContextLogoutHandler().logout(
                request,
                response,
                SecurityContextHolder.getContext().getAuthentication());
    }

}
