package me.eeshe.tempus.dto;

public record AuthenticationResponseDTO(
        String token,
        long expirationTimeMillis) {
}
