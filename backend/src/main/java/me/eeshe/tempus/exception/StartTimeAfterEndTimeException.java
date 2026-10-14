package me.eeshe.tempus.exception;

public class StartTimeAfterEndTimeException extends RuntimeException {
    private static final String ERROR_MESSAGE = "Time entry start time can't be after end time";

    public StartTimeAfterEndTimeException() {
        super(ERROR_MESSAGE);
    }
}
