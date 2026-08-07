package me.eeshe.tempus.service;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import me.eeshe.tempus.entity.User;
import me.eeshe.tempus.request.LoginRequest;
import me.eeshe.tempus.request.RegisterRequest;

public interface AuthenticationService {

    User registerUser(RegisterRequest registerRequest);

    User loginUser(LoginRequest loginRequest, HttpServletRequest request,
            HttpServletResponse response);

    void logoutUser(HttpServletRequest request, HttpServletResponse response);
}
