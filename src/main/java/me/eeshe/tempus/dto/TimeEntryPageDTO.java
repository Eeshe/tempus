package me.eeshe.tempus.dto;

import java.time.Instant;
import java.util.List;

public record TimeEntryPageDTO(
        List<TimeEntryDTO> content,
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
