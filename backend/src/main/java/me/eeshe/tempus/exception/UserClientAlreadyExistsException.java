package me.eeshe.tempus.exception;

public class UserClientAlreadyExistsException extends RuntimeException {
    private static final String ERROR_MESSAGE = "User %s already has a client named %s";

    private final long userId;
    private final String clientName;

    public UserClientAlreadyExistsException(long userId, String clientName) {
        super(String.format(ERROR_MESSAGE, userId, clientName));

        this.userId = userId;
        this.clientName = clientName;
    }

    public long getUserId() {
        return userId;
    }

    public String getClientName() {
        return clientName;
    }
}
