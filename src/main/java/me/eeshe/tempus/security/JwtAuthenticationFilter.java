package me.eeshe.tempus.security;

import java.io.IOException;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import me.eeshe.tempus.service.AuthenticationService;

public class JwtAuthenticationFilter extends OncePerRequestFilter {
    private final AuthenticationService authenticationService;

    public JwtAuthenticationFilter(AuthenticationService authenticationService) {
        this.authenticationService = authenticationService;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {
        final String token = extractToken(request);
        if (token != null) {
            final UserDetails userDetails = authenticationService.validateToken(token);
            final UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(
                    userDetails,
                    null,
                    userDetails.getAuthorities());

            SecurityContextHolder.getContext().setAuthentication(authenticationToken);
        }
        filterChain.doFilter(request, response);
    }

    private String extractToken(HttpServletRequest request) {
        final String bearerToken = request.getHeader("Authorization");
        if (bearerToken == null) {
            return null;
        }
        if (!bearerToken.startsWith("Bearer ")) {
            return null;
        }
        return bearerToken.substring(7);
    }

}
