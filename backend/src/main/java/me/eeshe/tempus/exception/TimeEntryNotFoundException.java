package me.eeshe.tempus.exception;

public class TimeEntryNotFoundException extends RuntimeException {
    private static final String ERROR_MESSAGE = "Time entry with ID %s does not exist";

    private final long timeEntryId;

    public TimeEntryNotFoundException(long timeEntryId) {
        super(String.format(ERROR_MESSAGE, timeEntryId));

        this.timeEntryId = timeEntryId;
    }

    public long getTimeEntryId() {
        return timeEntryId;
    }
}
