package me.eeshe.tempus.exception;

public class InvalidZoneIdException extends RuntimeException {
    private static final String ERROR_MESSAGE = "Invalid time zone id: %s";

    public InvalidZoneIdException(String zoneId) {
        super(ERROR_MESSAGE.formatted(zoneId));
    }
}
