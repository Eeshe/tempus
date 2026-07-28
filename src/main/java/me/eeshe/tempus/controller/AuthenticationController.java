package me.eeshe.tempus.controller;

import java.util.concurrent.TimeUnit;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import me.eeshe.tempus.dto.AuthenticationResponseDTO;
import me.eeshe.tempus.dto.LoginRequestDTO;
import me.eeshe.tempus.service.AuthenticationService;

@RestController
@RequestMapping(path = "api/v1/auth")
public class AuthenticationController {
    private final AuthenticationService authenticationService;

    public AuthenticationController(AuthenticationService authenticationService) {
        this.authenticationService = authenticationService;
    }

    @PostMapping
    public ResponseEntity<AuthenticationResponseDTO> authenticate(@RequestBody LoginRequestDTO loginRequestDTO) {
        final UserDetails userDetails = authenticationService.authenticate(
                loginRequestDTO.name(),
                loginRequestDTO.password());

        final String token = authenticationService.generateToken(userDetails);
        final AuthenticationResponseDTO authenticationResponseDTO = new AuthenticationResponseDTO(
                token,
                TimeUnit.HOURS.toMillis(24));

        return ResponseEntity.ok(authenticationResponseDTO);
    }
}
