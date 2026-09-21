package me.eeshe.tempus.request;

import java.math.BigDecimal;

import me.eeshe.tempus.entity.Client;
import me.eeshe.tempus.entity.User;

public record CreateProjectRequest(
        String name,
        User user,
        BigDecimal hourlyRate,
        Client client) {
}
