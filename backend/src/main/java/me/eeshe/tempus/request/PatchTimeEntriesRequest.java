package me.eeshe.tempus.request;

import java.util.List;

public record PatchTimeEntriesRequest(List<TimeEntryPatch> timeEntries) {
}
