package me.eeshe.tempus.request;

import java.util.List;

public record DeleteTimeEntriesRequest(List<Long> timeEntryIds) {
}
