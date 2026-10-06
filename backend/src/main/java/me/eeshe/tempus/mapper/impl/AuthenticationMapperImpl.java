package me.eeshe.tempus.mapper.impl;

import org.springframework.stereotype.Component;

import me.eeshe.tempus.dto.LoginRequestDTO;
import me.eeshe.tempus.dto.RegisterRequestDTO;
import me.eeshe.tempus.mapper.AuthenticationMapper;
import me.eeshe.tempus.request.LoginRequest;
import me.eeshe.tempus.request.RegisterRequest;

@Component
public class AuthenticationMapperImpl implements AuthenticationMapper {

    @Override
    public RegisterRequest fromDTO(RegisterRequestDTO registerRequestDTO) {
        return new RegisterRequest(
                registerRequestDTO.username(),
                registerRequestDTO.password());
    }

    @Override
    public LoginRequest fromDTO(LoginRequestDTO loginRequestDTO) {
        return new LoginRequest(
                loginRequestDTO.username(),
                loginRequestDTO.password());
    }
}
