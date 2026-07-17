package me.eeshe.tempus.exception;

public class ClientNotFoundException extends RuntimeException {
    private static final String ERROR_MESSAGE = "Client with ID %s does not exist";

    private final long clientId;

    public ClientNotFoundException(long clientId) {
        super(String.format(ERROR_MESSAGE, clientId));

        this.clientId = clientId;
    }

    public long getClientId() {
        return clientId;
    }
}
