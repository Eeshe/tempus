package me.eeshe.tempus.exception;

import java.util.List;

public class TimeEntriesNotFoundException extends RuntimeException {
    private static final String ERROR_MESSAGE = "Time entries with IDs %s do not exist";

    private final List<Long> timeEntryIds;

    public TimeEntriesNotFoundException(List<Long> timeEntryIds) {
        super(String.format(ERROR_MESSAGE, timeEntryIds));

        this.timeEntryIds = timeEntryIds;
    }

    public List<Long> getTimeEntryIds() {
        return timeEntryIds;
    }
}
