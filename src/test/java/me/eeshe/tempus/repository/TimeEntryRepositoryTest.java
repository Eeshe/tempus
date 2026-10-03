package me.eeshe.tempus.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import me.eeshe.tempus.entity.Client;
import me.eeshe.tempus.entity.Project;
import me.eeshe.tempus.entity.Task;
import me.eeshe.tempus.entity.TimeEntry;
import me.eeshe.tempus.entity.User;
import me.eeshe.tempus.repository.projection.DailyEntryCount;
import me.eeshe.tempus.specification.TimeEntrySpecification;
import me.eeshe.tempus.support.RepositoryTestBase;

public class TimeEntryRepositoryTest extends RepositoryTestBase {
    private static final long MILLIS_PER_DAY = 86_400_000L;

    private static final Instant TIME_08 = Instant.parse("2026-01-01T08:00:00Z");
    private static final Instant TIME_09 = Instant.parse("2026-01-01T09:00:00Z");
    private static final Instant TIME_10 = Instant.parse("2026-01-01T10:00:00Z");
    private static final Instant TIME_10_30 = Instant.parse("2026-01-01T10:30:00Z");
    private static final Instant TIME_11 = Instant.parse("2026-01-01T11:00:00Z");
    private static final Instant TIME_12 = Instant.parse("2026-01-01T12:00:00Z");
    private static final Instant TIME_13 = Instant.parse("2026-01-01T13:00:00Z");
    private static final Instant TIME_15 = Instant.parse("2026-01-01T15:00:00Z");

    private static final Instant RANGE_START = Instant.parse("2026-01-01T10:00:00Z");
    private static final Instant RANGE_END = Instant.parse("2026-01-01T12:00:00Z");

    private static final Instant DAY_ONE_START = Instant.parse("2026-01-01T08:00:00Z");
    private static final Instant DAY_TWO_START = Instant.parse("2026-01-02T09:00:00Z");
    private static final Instant DAY_THREE_START = Instant.parse("2026-01-03T10:00:00Z");
    private static final Instant DAY_ONE_LAST_MILLI = Instant.parse("2026-01-01T23:59:59.999Z");
    private static final Instant DAY_TWO_MIDNIGHT = Instant.parse("2026-01-02T00:00:00Z");

    private static long epochDay(Instant instant) {
        return instant.toEpochMilli() / MILLIS_PER_DAY;
    }

    @Nested
    class FindByIdAndUserId {

        @Test
        void returnsTimeEntryWhenOwnedByUser() {
            final User user = createUser("MyUser");
            final Project project = createProject("MyProject", user);
            final TimeEntry timeEntry = createTimeEntry(
                    user, project, null, "MyDescription", true, TIME_09, TIME_10);

            final Optional<TimeEntry> found = timeEntryRepository.findByIdAndUserId(timeEntry.getId(), user.getId());

            assertThat(found).contains(timeEntry);
        }

        @Test
        void returnsEmptyWhenOwnedByOtherUser() {
            final User user = createUser("MyUser");
            final User otherUser = createUser("MyOtherUser");
            final Project project = createProject("MyProject", user);
            final TimeEntry timeEntry = createTimeEntry(
                    user, project, null, "MyDescription", true, TIME_09, TIME_10);

            assertThat(timeEntryRepository.findByIdAndUserId(timeEntry.getId(), otherUser.getId())).isEmpty();
        }
    }

    @Nested
    class FindByUserIdAndStartTimeBetween {

        @Test
        void returnsEntriesInRangeOrderedByStartTimeDescending() {
            final User user = createUser("MyUser");
            final Project project = createProject("MyProject", user);
            final TimeEntry earlierEntry = createTimeEntry(
                    user, project, null, "MyDescription", true, TIME_10_30, TIME_11);
            final TimeEntry laterEntry = createTimeEntry(
                    user, project, null, "MyDescription", true, TIME_11, TIME_12);
            createTimeEntry(user, project, null, "MyDescription", true, TIME_09, TIME_09);

            final List<TimeEntry> timeEntries = timeEntryRepository
                    .findByUserIdAndStartTimeGreaterThanEqualAndStartTimeLessThanOrderByStartTimeDesc(
                            user.getId(), RANGE_START, RANGE_END);

            assertThat(timeEntries).containsExactly(laterEntry, earlierEntry);
        }

        @Test
        void includesEntryAtStartBoundary() {
            final User user = createUser("MyUser");
            final Project project = createProject("MyProject", user);
            final TimeEntry timeEntry = createTimeEntry(
                    user, project, null, "MyDescription", true, RANGE_START, TIME_11);

            final List<TimeEntry> timeEntries = timeEntryRepository
                    .findByUserIdAndStartTimeGreaterThanEqualAndStartTimeLessThanOrderByStartTimeDesc(
                            user.getId(), RANGE_START, RANGE_END);

            assertThat(timeEntries).containsExactly(timeEntry);
        }

        @Test
        void excludesEntryAtEndBoundary() {
            final User user = createUser("MyUser");
            final Project project = createProject("MyProject", user);
            createTimeEntry(user, project, null, "MyDescription", true, RANGE_END, TIME_13);

            final List<TimeEntry> timeEntries = timeEntryRepository
                    .findByUserIdAndStartTimeGreaterThanEqualAndStartTimeLessThanOrderByStartTimeDesc(
                            user.getId(), RANGE_START, RANGE_END);

            assertThat(timeEntries).isEmpty();
        }

        @Test
        void includesEntryWithNullEndTime() {
            final User user = createUser("MyUser");
            final Project project = createProject("MyProject", user);
            final TimeEntry timeEntry = createTimeEntry(
                    user, project, null, "MyDescription", true, TIME_10_30, null);

            final List<TimeEntry> timeEntries = timeEntryRepository
                    .findByUserIdAndStartTimeGreaterThanEqualAndStartTimeLessThanOrderByStartTimeDesc(
                            user.getId(), RANGE_START, RANGE_END);

            assertThat(timeEntries).containsExactly(timeEntry);
        }

        @Test
        void returnsOnlyUserEntries() {
            final User user = createUser("MyUser");
            final User otherUser = createUser("MyOtherUser");

            final Project project = createProject("MyProject", user);
            final Project otherProject = createProject("MyProject", otherUser);

            final TimeEntry timeEntry = createTimeEntry(
                    user, project, null, "MyDescription", true, TIME_10_30, TIME_11);
            createTimeEntry(otherUser, otherProject, null, "MyDescription", true, TIME_10_30, TIME_11);

            final List<TimeEntry> timeEntries = timeEntryRepository
                    .findByUserIdAndStartTimeGreaterThanEqualAndStartTimeLessThanOrderByStartTimeDesc(
                            user.getId(), RANGE_START, RANGE_END);

            assertThat(timeEntries).containsExactly(timeEntry);
        }
    }

    @Nested
    class CountAllEntriesByUtcEpochDay {

        @Test
        void returnsEntryCountsGroupedByUtcEpochDayDescending() {
            final User user = createUser("MyUser");
            final Project project = createProject("MyProject", user);

            createTimeEntry(user, project, null, "MyDescription", true, DAY_ONE_START, null);
            createTimeEntry(user, project, null, "MyDescription", true, DAY_TWO_START, null);
            createTimeEntry(user, project, null, "MyDescription", true, DAY_THREE_START, null);

            final List<DailyEntryCount> dailyEntryCounts = timeEntryRepository
                    .countAllEntriesByUtcEpochDay(user.getId());

            assertThat(dailyEntryCounts)
                    .extracting(DailyEntryCount::getEpochDay)
                    .containsExactly(epochDay(DAY_THREE_START), epochDay(DAY_TWO_START), epochDay(DAY_ONE_START));
            assertThat(dailyEntryCounts)
                    .extracting(DailyEntryCount::getEntryCount)
                    .containsExactly(1L, 1L, 1L);
        }

        @Test
        void aggregatesMultipleEntriesOnSameDay() {
            final User user = createUser("MyUser");
            final Project project = createProject("MyProject", user);

            createTimeEntry(user, project, null, "MyDescription", true, DAY_ONE_START, null);
            createTimeEntry(user, project, null, "MyDescription", true, DAY_ONE_START.plusSeconds(3600), null);

            final List<DailyEntryCount> dailyEntryCounts = timeEntryRepository
                    .countAllEntriesByUtcEpochDay(user.getId());

            assertThat(dailyEntryCounts).hasSize(1);
            assertThat(dailyEntryCounts.getFirst().getEpochDay()).isEqualTo(epochDay(DAY_ONE_START));
            assertThat(dailyEntryCounts.getFirst().getEntryCount()).isEqualTo(2L);
        }

        @Test
        void countsEntriesAtUtcMidnightBoundaryOnCorrectDay() {
            final User user = createUser("MyUser");
            final Project project = createProject("MyProject", user);

            createTimeEntry(user, project, null, "MyDescription", true, DAY_ONE_LAST_MILLI, null);
            createTimeEntry(user, project, null, "MyDescription", true, DAY_TWO_MIDNIGHT, null);

            final List<DailyEntryCount> dailyEntryCounts = timeEntryRepository
                    .countAllEntriesByUtcEpochDay(user.getId());

            assertThat(dailyEntryCounts)
                    .extracting(DailyEntryCount::getEpochDay)
                    .containsExactly(epochDay(DAY_TWO_MIDNIGHT), epochDay(DAY_ONE_LAST_MILLI));
            assertThat(dailyEntryCounts)
                    .extracting(DailyEntryCount::getEntryCount)
                    .containsExactly(1L, 1L);
        }

        @Test
        void returnsOnlyUserEntries() {
            final User user = createUser("MyUser");
            final User otherUser = createUser("MyOtherUser");

            final Project project = createProject("MyProject", user);
            final Project otherProject = createProject("MyProject", otherUser);

            createTimeEntry(user, project, null, "MyDescription", true, DAY_ONE_START, null);
            createTimeEntry(otherUser, otherProject, null, "MyDescription", true, DAY_ONE_START, null);

            final List<DailyEntryCount> dailyEntryCounts = timeEntryRepository
                    .countAllEntriesByUtcEpochDay(user.getId());

            assertThat(dailyEntryCounts).hasSize(1);
            assertThat(dailyEntryCounts.getFirst().getEntryCount()).isEqualTo(1L);
        }

        @Test
        void returnsEmptyWhenUserHasNoEntries() {
            final User user = createUser("MyUser");

            assertThat(timeEntryRepository.countAllEntriesByUtcEpochDay(user.getId())).isEmpty();
        }
    }

    @Nested
    class FindAllWithSpecification {

        @Test
        void filtersByUserAndExcludesNullEndTime() {
            final User user = createUser("MyUser");
            final User otherUser = createUser("MyOtherUser");

            final Project project = createProject("MyProject", user);
            final Project otherProject = createProject("MyProject", otherUser);

            final TimeEntry completedEntry = createTimeEntry(
                    user, project, null, "MyDescription", true, TIME_09, TIME_10);
            createTimeEntry(user, project, null, "MyDescription", true, TIME_10, null);
            createTimeEntry(otherUser, otherProject, null, "MyDescription", true, TIME_09, TIME_10);

            final List<TimeEntry> timeEntries = timeEntryRepository.findAll(TimeEntrySpecification
                    .withFilters(user.getId(), null, null, null, null, null, null, null));

            assertThat(timeEntries).containsExactly(completedEntry);
        }

        @Test
        void filtersByDateRange() {
            final User user = createUser("MyUser");
            final Project project = createProject("MyProject", user);

            createTimeEntry(user, project, null, "MyDescription", true, TIME_08, TIME_09);
            final TimeEntry matchingEntry = createTimeEntry(
                    user, project, null, "MyDescription", true, TIME_10_30, TIME_11);
            createTimeEntry(user, project, null, "MyDescription", true, TIME_11, TIME_13);
            createTimeEntry(user, project, null, "MyDescription", true, TIME_08, TIME_11);

            final List<TimeEntry> timeEntries = timeEntryRepository.findAll(TimeEntrySpecification
                    .withFilters(user.getId(), TIME_09, TIME_12, null, null, null, null, null));

            assertThat(timeEntries).containsExactly(matchingEntry);
        }

        @Test
        void includesEntriesAtDateRangeBoundaries() {
            final User user = createUser("MyUser");
            final Project project = createProject("MyProject", user);
            final TimeEntry startBoundaryEntry = createTimeEntry(
                    user, project, null, "MyDescription", true, TIME_09, TIME_10);
            final TimeEntry endBoundaryEntry = createTimeEntry(
                    user, project, null, "MyDescription", true, TIME_11, TIME_12);

            final List<TimeEntry> timeEntries = timeEntryRepository.findAll(TimeEntrySpecification
                    .withFilters(user.getId(), TIME_09, TIME_12, null, null, null, null, null));

            assertThat(timeEntries).containsExactlyInAnyOrder(startBoundaryEntry, endBoundaryEntry);
        }

        @Test
        void appliesDateRangeOnlyWhenBothBoundsProvided() {
            final User user = createUser("MyUser");
            final Project project = createProject("MyProject", user);
            final TimeEntry timeEntry = createTimeEntry(
                    user, project, null, "MyDescription", true, TIME_15, TIME_15.plusSeconds(3600));

            final List<TimeEntry> timeEntries = timeEntryRepository.findAll(TimeEntrySpecification
                    .withFilters(user.getId(), TIME_09, null, null, null, null, null, null));

            assertThat(timeEntries).containsExactly(timeEntry);
        }

        @Test
        void filtersByProjectIds() {
            final User user = createUser("MyUser");

            final Project project = createProject("MyProject", user);
            final Project otherProject = createProject("MyOtherProject", user);

            final TimeEntry timeEntry = createTimeEntry(
                    user, project, null, "MyDescription", true, TIME_09, TIME_10);
            createTimeEntry(user, otherProject, null, "MyDescription", true, TIME_09, TIME_10);

            final List<TimeEntry> timeEntries = timeEntryRepository.findAll(TimeEntrySpecification
                    .withFilters(user.getId(), null, null, List.of(project.getId()), null, null, null, null));

            assertThat(timeEntries).containsExactly(timeEntry);
        }

        @Test
        void filtersByTaskIds() {
            final User user = createUser("MyUser");
            final Project project = createProject("MyProject", user);

            final Task task = createTask("MyTask", user, project);
            final Task otherTask = createTask("MyOtherTask", user, project);

            final TimeEntry timeEntry = createTimeEntry(
                    user, project, task, "MyDescription", true, TIME_09, TIME_10);
            createTimeEntry(user, project, otherTask, "MyDescription", true, TIME_09, TIME_10);
            createTimeEntry(user, project, null, "MyDescription", true, TIME_09, TIME_10);

            final List<TimeEntry> timeEntries = timeEntryRepository.findAll(TimeEntrySpecification
                    .withFilters(user.getId(), null, null, null, List.of(task.getId()), null, null, null));

            assertThat(timeEntries).containsExactly(timeEntry);
        }

        @Test
        void filtersByClientIds() {
            final User user = createUser("MyUser");

            final Client client = createClient("MyClient", user);
            final Client otherClient = createClient("MyOtherClient", user);

            final Project project = createProject("MyProject", user, null, client);
            final Project otherProject = createProject("MyOtherProject", user, null, otherClient);
            final Project clientlessProject = createProject("MyClientlessProject", user);

            final TimeEntry timeEntry = createTimeEntry(
                    user, project, null, "MyDescription", true, TIME_09, TIME_10);
            createTimeEntry(user, otherProject, null, "MyDescription", true, TIME_09, TIME_10);
            createTimeEntry(user, clientlessProject, null, "MyDescription", true, TIME_09, TIME_10);

            final List<TimeEntry> timeEntries = timeEntryRepository.findAll(TimeEntrySpecification
                    .withFilters(user.getId(), null, null, null, null, List.of(client.getId()), null, null));

            assertThat(timeEntries).containsExactly(timeEntry);
        }

        @Test
        void filtersByDescriptions() {
            final User user = createUser("MyUser");
            final Project project = createProject("MyProject", user);

            final TimeEntry timeEntry = createTimeEntry(
                    user, project, null, "MyDescription", true, TIME_09, TIME_10);
            createTimeEntry(user, project, null, "MyOtherDescription", true, TIME_09, TIME_10);
            createTimeEntry(user, project, null, null, true, TIME_09, TIME_10);

            final List<TimeEntry> timeEntries = timeEntryRepository.findAll(TimeEntrySpecification
                    .withFilters(user.getId(), null, null, null, null, null, List.of("MyDescription"), null));

            assertThat(timeEntries).containsExactly(timeEntry);
        }

        @Test
        void filtersByBillable() {
            final User user = createUser("MyUser");
            final Project project = createProject("MyProject", user);
            final TimeEntry timeEntry = createTimeEntry(
                    user, project, null, "MyDescription", true, TIME_09, TIME_10);
            createTimeEntry(user, project, null, "MyDescription", false, TIME_09, TIME_10);

            final List<TimeEntry> timeEntries = timeEntryRepository.findAll(TimeEntrySpecification
                    .withFilters(user.getId(), null, null, null, null, null, null, true));

            assertThat(timeEntries).containsExactly(timeEntry);
        }

        @Test
        void filtersByNonBillable() {
            final User user = createUser("MyUser");
            final Project project = createProject("MyProject", user);

            createTimeEntry(user, project, null, "MyDescription", true, TIME_09, TIME_10);
            final TimeEntry timeEntry = createTimeEntry(
                    user, project, null, "MyDescription", false, TIME_09, TIME_10);

            final List<TimeEntry> timeEntries = timeEntryRepository.findAll(TimeEntrySpecification
                    .withFilters(user.getId(), null, null, null, null, null, null, false));

            assertThat(timeEntries).containsExactly(timeEntry);
        }

        @Test
        void ignoresEmptyFilterLists() {
            final User user = createUser("MyUser");
            final Project project = createProject("MyProject", user);

            final TimeEntry firstTimeEntry = createTimeEntry(
                    user, project, null, "MyDescription", true, TIME_09, TIME_10);
            final TimeEntry secondTimeEntry = createTimeEntry(
                    user, project, null, "MyOtherDescription", false, TIME_11, TIME_12);
            createTimeEntry(user, project, null, "MyDescription", true, TIME_13, null);

            final List<TimeEntry> timeEntries = timeEntryRepository.findAll(TimeEntrySpecification
                    .withFilters(user.getId(), null, null, List.of(), List.of(), List.of(), List.of(), null));

            assertThat(timeEntries).containsExactlyInAnyOrder(firstTimeEntry, secondTimeEntry);
        }
    }

    @Nested
    class SaveTimeEntry {

        @Test
        void savePersistsTimeEntryAndSetsGeneratedFields() {
            final User user = createUser("MyUser");
            final Project project = createProject("MyProject", user);
            final Task task = createTask("MyTask", user, project);

            final TimeEntry saved = timeEntryRepository.save(new TimeEntry(
                    user, project, task, "MyDescription", true, TIME_09, TIME_10));

            assertThat(saved.getId()).isPositive();
            assertThat(saved.getCreatedAt()).isNotNull();

            entityManager.flush();
            entityManager.clear();

            final Optional<TimeEntry> found = timeEntryRepository.findById(saved.getId());

            assertThat(found).isPresent();
            assertThat(found.get().getDescription()).isEqualTo("MyDescription");
            assertThat(found.get().isBillable()).isTrue();
            assertThat(found.get().getStartTime()).isEqualTo(TIME_09);
            assertThat(found.get().getEndTime()).isEqualTo(TIME_10);
            assertThat(found.get().getProject()).isEqualTo(project);
            assertThat(found.get().getTask()).isEqualTo(task);
        }
    }

    @Nested
    class DeleteTimeEntry {

        @Test
        void deleteRemovesTimeEntry() {
            final User user = createUser("MyUser");
            final Project project = createProject("MyProject", user);
            final TimeEntry timeEntry = createTimeEntry(
                    user, project, null, "MyDescription", true, TIME_09, TIME_10);

            timeEntryRepository.deleteById(timeEntry.getId());

            assertThat(timeEntryRepository.findByIdAndUserId(timeEntry.getId(), user.getId())).isEmpty();
        }

        @Test
        void deleteLeavesUserIntact() {
            final User user = createUser("MyUser");
            final Project project = createProject("MyProject", user);
            final TimeEntry timeEntry = createTimeEntry(
                    user, project, null, "MyDescription", true, TIME_09, TIME_10);

            timeEntryRepository.deleteById(timeEntry.getId());
            entityManager.flush();
            entityManager.clear();

            assertThat(userRepository.findById(user.getId())).isPresent();
        }

        @Test
        void deleteLeavesProjectIntact() {
            final User user = createUser("MyUser");
            final Project project = createProject("MyProject", user);
            final TimeEntry timeEntry = createTimeEntry(
                    user, project, null, "MyDescription", true, TIME_09, TIME_10);

            timeEntryRepository.deleteById(timeEntry.getId());
            entityManager.flush();
            entityManager.clear();

            assertThat(projectRepository.findById(project.getId())).isPresent();
        }

        @Test
        void deleteLeavesTaskIntact() {
            final User user = createUser("MyUser");
            final Project project = createProject("MyProject", user);
            final Task task = createTask("MyTask", user, project);
            final TimeEntry timeEntry = createTimeEntry(
                    user, project, task, "MyDescription", true, TIME_09, TIME_10);

            timeEntryRepository.deleteById(timeEntry.getId());
            entityManager.flush();
            entityManager.clear();

            assertThat(taskRepository.findById(task.getId())).isPresent();
        }
    }
}
