package me.eeshe.tempus.service;

import me.eeshe.tempus.entity.User;
import me.eeshe.tempus.request.LoginRequest;
import me.eeshe.tempus.request.RegisterRequest;

public interface AuthenticationService {

    User registerUser(RegisterRequest registerRequest);

    User loginUser(LoginRequest loginRequest);
}
