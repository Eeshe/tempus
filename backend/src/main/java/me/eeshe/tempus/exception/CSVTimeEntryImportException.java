package me.eeshe.tempus.exception;

import java.time.LocalDate;

public class CSVTimeEntryImportException extends RuntimeException {
    private static final String ERROR_MESSAGE = "CSV Time entry with date %s couldn't be imported: %s";

    private final LocalDate timeEntryDate;

    public CSVTimeEntryImportException(LocalDate timeEntryDate, String message) {
        super(String.format(ERROR_MESSAGE, timeEntryDate.toString(), message));

        this.timeEntryDate = timeEntryDate;
    }

    public LocalDate getTimeEntryDate() {
        return timeEntryDate;
    }
}
