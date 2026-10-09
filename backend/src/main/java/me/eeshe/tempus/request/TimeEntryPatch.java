package me.eeshe.tempus.request;

public record TimeEntryPatch(long timeEntryId, PatchTimeEntryRequest patch) {
}
