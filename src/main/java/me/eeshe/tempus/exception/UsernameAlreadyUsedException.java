package me.eeshe.tempus.exception;

public class UsernameAlreadyUsedException extends RuntimeException {
    private static final String ERROR_MESSAGE = "Username %s is already used by another user";

    private final String username;

    public UsernameAlreadyUsedException(String username) {
        super(String.format(ERROR_MESSAGE, username));

        this.username = username;
    }

    public String getUsername() {
        return username;
    }
}
