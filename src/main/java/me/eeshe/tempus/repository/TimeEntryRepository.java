package me.eeshe.tempus.repository;

import java.time.Instant;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

import me.eeshe.tempus.entity.TimeEntry;
import me.eeshe.tempus.repository.projection.DailyEntryCount;

public interface TimeEntryRepository extends JpaRepository<TimeEntry, Long>, JpaSpecificationExecutor<TimeEntry> {

    /**
     * Counts time entries grouped by their UTC epoch day. Time entries are stored
     * with their start time as epoch milliseconds, so integer division yields the
     * epoch day.
     */
    @Query(value = """
            SELECT start_time / 86400000 AS epochDay, COUNT(*) AS entryCount
            FROM time_entries
            GROUP BY epochDay
            ORDER BY epochDay DESC
            """, nativeQuery = true)
    List<DailyEntryCount> countAllEntriesByUtcEpochDay();

    List<TimeEntry> findByStartTimeGreaterThanEqualAndStartTimeLessThanOrderByStartTimeDesc(
            Instant startInclusive,
            Instant endExclusive);
}
