package me.eeshe.tempus.model;

import java.time.Instant;
import java.util.List;

import me.eeshe.tempus.entity.TimeEntry;

public record TimeEntryPage(
        List<TimeEntry> content,
        Instant previousCursor,
        Instant currentCursor,
        Instant nextCursor,
        int page,
        int size,
        long totalElements,
        int totalPages,
        boolean first,
        boolean last) {
}
