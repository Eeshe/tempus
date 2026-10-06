package me.eeshe.tempus.model;

import me.eeshe.tempus.entity.Client;

public record ClientReportEntry(
        Client client,
        long trackedTimeMillis) {
}
