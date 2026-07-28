package me.eeshe.tempus.service.impl;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import me.eeshe.tempus.service.AuthenticationService;

@Service
public class AuthenticationServiceImpl implements AuthenticationService {
    private final AuthenticationManager authenticationManager;
    private final UserDetailsService userDetailsService;

    private final long jwtExpirationMillis = TimeUnit.HOURS.toMillis(24);

    @Value("${jwt.secret}")
    private String secretKey;

    public AuthenticationServiceImpl(AuthenticationManager authenticationManager,
            UserDetailsService userDetailsService) {
        this.authenticationManager = authenticationManager;
        this.userDetailsService = userDetailsService;
    }

    @Override
    public UserDetails authenticate(String email, String password) {
        authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(email, password));

        return userDetailsService.loadUserByUsername(email);
    }

    @Override
    public String generateToken(UserDetails userDetails) {
        final Map<String, Object> claims = new HashMap<>();

        return Jwts.builder()
                .claims(claims)
                .subject(userDetails.getUsername())
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + jwtExpirationMillis))
                .signWith(getSigningKey())
                .compact();
    }

    @Override
    public UserDetails validateToken(String token) {
        String username = extractUsername(token);

        return userDetailsService.loadUserByUsername(username);
    }

    private String extractUsername(String token) {
        Claims claims = Jwts.parser()
                .verifyWith(getSigningKey()).build()
                .parseSignedClaims(token)
                .getPayload();

        return claims.getSubject();
    }

    private SecretKey getSigningKey() {
        byte[] keyBytes = secretKey.getBytes();

        return Keys.hmacShaKeyFor(keyBytes);
    }
}
