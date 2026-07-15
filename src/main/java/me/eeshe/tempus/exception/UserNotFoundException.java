package me.eeshe.tempus.exception;

public class UserNotFoundException extends RuntimeException {
    private static final String ERROR_MESSAGE = "User with ID %s does not exist";

    private final long userId;

    public UserNotFoundException(long userId) {
        super(String.format(ERROR_MESSAGE, userId));

        this.userId = userId;
    }

    public long getUserId() {
        return userId;
    }
}
