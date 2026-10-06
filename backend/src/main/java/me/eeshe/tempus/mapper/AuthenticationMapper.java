package me.eeshe.tempus.mapper;

import me.eeshe.tempus.dto.LoginRequestDTO;
import me.eeshe.tempus.dto.RegisterRequestDTO;
import me.eeshe.tempus.request.LoginRequest;
import me.eeshe.tempus.request.RegisterRequest;

public interface AuthenticationMapper {

    RegisterRequest fromDTO(RegisterRequestDTO registerRequestDTO);

    LoginRequest fromDTO(LoginRequestDTO loginRequestDTO);
}
