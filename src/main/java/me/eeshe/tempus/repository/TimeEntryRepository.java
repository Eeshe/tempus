package me.eeshe.tempus.repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import me.eeshe.tempus.entity.TimeEntry;
import me.eeshe.tempus.repository.projection.DailyEntryCount;

public interface TimeEntryRepository extends JpaRepository<TimeEntry, Long>, JpaSpecificationExecutor<TimeEntry> {

    Optional<TimeEntry> findByIdAndUserId(long id, long userId);

    /**
     * Counts time entries grouped by their UTC epoch day. Time entries are stored
     * with their start time as epoch milliseconds, so integer division yields the
     * epoch day.
     */
    @Query(value = """
            SELECT start_time / 86400000 AS epochDay, COUNT(*) AS entryCount
            FROM time_entries
            WHERE user_id = :userId
            GROUP BY epochDay
            ORDER BY epochDay DESC
            """, nativeQuery = true)
    List<DailyEntryCount> countAllEntriesByUtcEpochDay(@Param("userId") long userId);

    List<TimeEntry> findByUserIdAndStartTimeGreaterThanEqualAndStartTimeLessThanOrderByStartTimeDesc(
            long userId,
            Instant startInclusive,
            Instant endExclusive);
}
