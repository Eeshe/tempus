package me.eeshe.tempus.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openapitools.jackson.nullable.JsonNullable;
import org.springframework.test.util.ReflectionTestUtils;

import me.eeshe.tempus.entity.Project;
import me.eeshe.tempus.entity.Task;
import me.eeshe.tempus.entity.TimeEntry;
import me.eeshe.tempus.entity.User;
import me.eeshe.tempus.exception.TimeEntryNotFoundException;
import me.eeshe.tempus.model.TimeEntryPage;
import me.eeshe.tempus.repository.TimeEntryRepository;
import me.eeshe.tempus.repository.projection.DailyEntryCount;
import me.eeshe.tempus.request.CreateTimeEntryRequest;
import me.eeshe.tempus.request.PatchTimeEntryRequest;
import me.eeshe.tempus.support.EntityTestBase;

@ExtendWith(MockitoExtension.class)
public class TimeEntryServiceImplTest extends EntityTestBase {
    private static final long TIME_ENTRY_ID = 3L;
    private static final long MILLIS_PER_DAY = 86_400_000L;

    private static final long DAY_ONE = 20_453L;
    private static final long DAY_TWO = 20_454L;
    private static final long DAY_THREE = 20_455L;
    private static final long DAY_FOUR = 20_456L;

    private static final Instant START_TIME = Instant.parse("2026-01-01T09:00:00Z");
    private static final Instant END_TIME = Instant.parse("2026-01-01T10:00:00Z");

    @Mock
    private TimeEntryRepository timeEntryRepository;

    @InjectMocks
    private TimeEntryServiceImpl timeEntryService;

    @Nested
    class ListTimeEntries {

        @Test
        void clampsMinPageSizeToOne() {
            when(timeEntryRepository.countAllEntriesByUtcEpochDay(USER_ID)).thenReturn(List.of());

            final TimeEntryPage timeEntryPage = timeEntryService.listTimeEntries(USER_ID, dayStart(DAY_TWO), 0);

            assertThat(timeEntryPage.content()).isEmpty();
            assertThat(timeEntryPage.previousCursor()).isNull();
            assertThat(timeEntryPage.currentCursor()).isEqualTo(dayStart(DAY_TWO));
            assertThat(timeEntryPage.nextCursor()).isNull();
            assertThat(timeEntryPage.page()).isZero();
            assertThat(timeEntryPage.size()).isEqualTo(1);
            assertThat(timeEntryPage.totalElements()).isZero();
            assertThat(timeEntryPage.totalPages()).isZero();
            assertThat(timeEntryPage.first()).isTrue();
            assertThat(timeEntryPage.last()).isTrue();
        }

        @Test
        void returnsEmptyPageWhenCursorOlderThanAllDays() {
            when(timeEntryRepository.countAllEntriesByUtcEpochDay(USER_ID)).thenReturn(createDayGroups());

            final TimeEntryPage timeEntryPage = timeEntryService.listTimeEntries(USER_ID, dayStart(DAY_ONE - 1), 3);

            assertThat(timeEntryPage.content()).isEmpty();
            assertThat(timeEntryPage.previousCursor()).isEqualTo(dayStart(DAY_ONE));
            assertThat(timeEntryPage.currentCursor()).isEqualTo(dayStart(DAY_ONE - 1));
            assertThat(timeEntryPage.nextCursor()).isNull();
            assertThat(timeEntryPage.page()).isEqualTo(3);
            assertThat(timeEntryPage.size()).isEqualTo(3);
            assertThat(timeEntryPage.totalElements()).isEqualTo(7);
            assertThat(timeEntryPage.totalPages()).isEqualTo(3);
            assertThat(timeEntryPage.first()).isFalse();
            assertThat(timeEntryPage.last()).isTrue();

            verify(timeEntryRepository, never())
                    .findByUserIdAndStartTimeGreaterThanEqualAndStartTimeLessThanOrderByStartTimeDesc(
                            anyLong(), any(), any());
        }

        @Test
        void returnsSinglePageWhenFewerEntriesThanPageSize() {
            when(timeEntryRepository.countAllEntriesByUtcEpochDay(USER_ID))
                    .thenReturn(List.of(createDayGroup(DAY_TWO, 2)));
            final TimeEntry timeEntry = createTimeEntry(TIME_ENTRY_ID);

            when(timeEntryRepository.findByUserIdAndStartTimeGreaterThanEqualAndStartTimeLessThanOrderByStartTimeDesc(
                    USER_ID, dayStart(DAY_TWO), dayStart(DAY_TWO + 1))).thenReturn(List.of(timeEntry));

            final TimeEntryPage timeEntryPage = timeEntryService.listTimeEntries(USER_ID, dayStart(DAY_TWO), 5);

            assertThat(timeEntryPage.content()).containsExactly(timeEntry);
            assertThat(timeEntryPage.previousCursor()).isNull();
            assertThat(timeEntryPage.currentCursor()).isEqualTo(dayStart(DAY_TWO));
            assertThat(timeEntryPage.nextCursor()).isNull();
            assertThat(timeEntryPage.page()).isZero();
            assertThat(timeEntryPage.size()).isEqualTo(5);
            assertThat(timeEntryPage.totalElements()).isEqualTo(2);
            assertThat(timeEntryPage.totalPages()).isEqualTo(1);
            assertThat(timeEntryPage.first()).isTrue();
            assertThat(timeEntryPage.last()).isTrue();
        }

        @Test
        void returnsFirstPageWithNextCursor() {
            when(timeEntryRepository.countAllEntriesByUtcEpochDay(USER_ID)).thenReturn(createDayGroups());
            final TimeEntry timeEntry = createTimeEntry(TIME_ENTRY_ID);

            when(timeEntryRepository.findByUserIdAndStartTimeGreaterThanEqualAndStartTimeLessThanOrderByStartTimeDesc(
                    USER_ID, dayStart(DAY_FOUR), dayStart(DAY_FOUR + 1))).thenReturn(List.of(timeEntry));

            final TimeEntryPage timeEntryPage = timeEntryService.listTimeEntries(USER_ID, dayStart(DAY_FOUR), 3);

            assertThat(timeEntryPage.content()).containsExactly(timeEntry);
            assertThat(timeEntryPage.previousCursor()).isNull();
            assertThat(timeEntryPage.currentCursor()).isEqualTo(dayStart(DAY_FOUR));
            assertThat(timeEntryPage.nextCursor()).isEqualTo(dayStart(DAY_THREE));
            assertThat(timeEntryPage.page()).isZero();
            assertThat(timeEntryPage.size()).isEqualTo(3);
            assertThat(timeEntryPage.totalElements()).isEqualTo(7);
            assertThat(timeEntryPage.totalPages()).isEqualTo(3);
            assertThat(timeEntryPage.first()).isTrue();
            assertThat(timeEntryPage.last()).isFalse();
        }

        @Test
        void returnsMiddlePageWithBothCursors() {
            when(timeEntryRepository.countAllEntriesByUtcEpochDay(USER_ID)).thenReturn(createDayGroups());
            final TimeEntry timeEntry = createTimeEntry(TIME_ENTRY_ID);

            when(timeEntryRepository.findByUserIdAndStartTimeGreaterThanEqualAndStartTimeLessThanOrderByStartTimeDesc(
                    USER_ID, dayStart(DAY_TWO), dayStart(DAY_FOUR))).thenReturn(List.of(timeEntry));

            final TimeEntryPage timeEntryPage = timeEntryService.listTimeEntries(USER_ID, dayStart(DAY_THREE), 3);

            assertThat(timeEntryPage.content()).containsExactly(timeEntry);
            assertThat(timeEntryPage.previousCursor()).isEqualTo(dayStart(DAY_FOUR));
            assertThat(timeEntryPage.currentCursor()).isEqualTo(dayStart(DAY_THREE));
            assertThat(timeEntryPage.nextCursor()).isEqualTo(dayStart(DAY_ONE));
            assertThat(timeEntryPage.page()).isEqualTo(1);
            assertThat(timeEntryPage.size()).isEqualTo(3);
            assertThat(timeEntryPage.totalElements()).isEqualTo(7);
            assertThat(timeEntryPage.totalPages()).isEqualTo(3);
            assertThat(timeEntryPage.first()).isFalse();
            assertThat(timeEntryPage.last()).isFalse();
        }

        @Test
        void returnsLastPageWhenCursorEqualsOldestDay() {
            when(timeEntryRepository.countAllEntriesByUtcEpochDay(USER_ID)).thenReturn(createDayGroups());
            final TimeEntry timeEntry = createTimeEntry(TIME_ENTRY_ID);

            when(timeEntryRepository.findByUserIdAndStartTimeGreaterThanEqualAndStartTimeLessThanOrderByStartTimeDesc(
                    USER_ID, dayStart(DAY_ONE), dayStart(DAY_ONE + 1))).thenReturn(List.of(timeEntry));

            final TimeEntryPage timeEntryPage = timeEntryService.listTimeEntries(USER_ID, dayStart(DAY_ONE), 3);

            assertThat(timeEntryPage.content()).containsExactly(timeEntry);
            assertThat(timeEntryPage.previousCursor()).isEqualTo(dayStart(DAY_THREE));
            assertThat(timeEntryPage.currentCursor()).isEqualTo(dayStart(DAY_ONE));
            assertThat(timeEntryPage.nextCursor()).isNull();
            assertThat(timeEntryPage.page()).isEqualTo(2);
            assertThat(timeEntryPage.size()).isEqualTo(3);
            assertThat(timeEntryPage.totalElements()).isEqualTo(7);
            assertThat(timeEntryPage.totalPages()).isEqualTo(3);
            assertThat(timeEntryPage.first()).isFalse();
            assertThat(timeEntryPage.last()).isTrue();
        }
    }

    @Nested
    class GetTimeEntry {

        @Test
        void returnsTimeEntryWhenFound() {
            final TimeEntry timeEntry = createTimeEntry(TIME_ENTRY_ID);
            when(timeEntryRepository.findByIdAndUserId(TIME_ENTRY_ID, USER_ID)).thenReturn(Optional.of(timeEntry));

            assertThat(timeEntryService.getTimeEntry(USER_ID, TIME_ENTRY_ID)).isEqualTo(timeEntry);
        }

        @Test
        void throwsWhenTimeEntryNotFound() {
            when(timeEntryRepository.findByIdAndUserId(TIME_ENTRY_ID, USER_ID)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> timeEntryService.getTimeEntry(USER_ID, TIME_ENTRY_ID))
                    .isInstanceOf(TimeEntryNotFoundException.class)
                    .hasMessage("Time entry with ID 3 does not exist");
        }
    }

    @Nested
    class CreateTimeEntry {

        @Test
        void savesTimeEntryWithRequestFields() {
            final User user = createUser(USER_ID);
            final Project project = createProject();
            final Task task = createTask();
            final CreateTimeEntryRequest createTimeEntryRequest = new CreateTimeEntryRequest(
                    user, project, task, "MyDescription", true, START_TIME, END_TIME);
            final TimeEntry savedTimeEntry = createTimeEntry(TIME_ENTRY_ID);

            when(timeEntryRepository.save(any(TimeEntry.class))).thenReturn(savedTimeEntry);

            final TimeEntry timeEntry = timeEntryService.createTimeEntry(createTimeEntryRequest);

            final ArgumentCaptor<TimeEntry> timeEntryCaptor = ArgumentCaptor.forClass(TimeEntry.class);

            verify(timeEntryRepository).save(timeEntryCaptor.capture());

            assertThat(timeEntryCaptor.getValue().getUser()).isEqualTo(user);
            assertThat(timeEntryCaptor.getValue().getProject()).isEqualTo(project);
            assertThat(timeEntryCaptor.getValue().getTask()).isEqualTo(task);
            assertThat(timeEntryCaptor.getValue().getDescription()).isEqualTo("MyDescription");
            assertThat(timeEntryCaptor.getValue().isBillable()).isTrue();
            assertThat(timeEntryCaptor.getValue().getStartTime()).isEqualTo(START_TIME);
            assertThat(timeEntryCaptor.getValue().getEndTime()).isEqualTo(END_TIME);
            assertThat(timeEntry).isEqualTo(savedTimeEntry);
        }
    }

    @Nested
    class CreateTimeEntries {

        @Test
        void savesAllTimeEntries() {
            final CreateTimeEntryRequest firstRequest = new CreateTimeEntryRequest(
                    createUser(USER_ID), createProject(), null, "MyDescription", true, START_TIME, END_TIME);
            final CreateTimeEntryRequest secondRequest = new CreateTimeEntryRequest(
                    createUser(USER_ID), createProject(), null, "MyDescription", false, START_TIME, END_TIME);
            final List<TimeEntry> savedTimeEntries = List.of(createTimeEntry(TIME_ENTRY_ID), createTimeEntry(TIME_ENTRY_ID + 1));

            when(timeEntryRepository.saveAll(anyList())).thenReturn(savedTimeEntries);

            final List<TimeEntry> timeEntries = timeEntryService.createTimeEntries(
                    List.of(firstRequest, secondRequest));

            final ArgumentCaptor<Iterable<TimeEntry>> timeEntryCaptor = ArgumentCaptor.forClass(Iterable.class);

            verify(timeEntryRepository).saveAll(timeEntryCaptor.capture());

            assertThat(timeEntryCaptor.getValue()).hasSize(2);
            assertThat(timeEntries).containsExactlyElementsOf(savedTimeEntries);
        }
    }

    @Nested
    class PatchTimeEntry {

        @Test
        void patchesProjectAndSaves() {
            final User user = createUser(USER_ID);
            final Project project = createProject();
            final Task task = createTask();
            final TimeEntry timeEntry = new TimeEntry(user, project, task, "MyDescription", true, START_TIME, END_TIME);
            final Project newProject = createSecondProject();

            when(timeEntryRepository.findByIdAndUserId(TIME_ENTRY_ID, USER_ID)).thenReturn(Optional.of(timeEntry));
            when(timeEntryRepository.save(timeEntry)).thenReturn(timeEntry);

            final TimeEntry patchedTimeEntry = timeEntryService.patchTimeEntry(USER_ID, TIME_ENTRY_ID,
                    new PatchTimeEntryRequestBuilder().project(newProject).build());

            assertThat(patchedTimeEntry.getProject()).isEqualTo(newProject);
            assertThat(patchedTimeEntry.getTask()).isEqualTo(task);
            assertThat(patchedTimeEntry.getDescription()).isEqualTo("MyDescription");
            assertThat(patchedTimeEntry.isBillable()).isTrue();
            assertThat(patchedTimeEntry.getStartTime()).isEqualTo(START_TIME);
            assertThat(patchedTimeEntry.getEndTime()).isEqualTo(END_TIME);

            verify(timeEntryRepository).save(timeEntry);
        }

        @Test
        void patchesTaskAndSaves() {
            final User user = createUser(USER_ID);
            final Project project = createProject();
            final Task task = createTask();
            final TimeEntry timeEntry = new TimeEntry(user, project, task, "MyDescription", true, START_TIME, END_TIME);
            final Task newTask = createTask();

            when(timeEntryRepository.findByIdAndUserId(TIME_ENTRY_ID, USER_ID)).thenReturn(Optional.of(timeEntry));
            when(timeEntryRepository.save(timeEntry)).thenReturn(timeEntry);

            final TimeEntry patchedTimeEntry = timeEntryService.patchTimeEntry(USER_ID, TIME_ENTRY_ID,
                    new PatchTimeEntryRequestBuilder().task(newTask).build());

            assertThat(patchedTimeEntry.getProject()).isEqualTo(project);
            assertThat(patchedTimeEntry.getTask()).isEqualTo(newTask);
            assertThat(patchedTimeEntry.getDescription()).isEqualTo("MyDescription");
            assertThat(patchedTimeEntry.isBillable()).isTrue();
            assertThat(patchedTimeEntry.getStartTime()).isEqualTo(START_TIME);
            assertThat(patchedTimeEntry.getEndTime()).isEqualTo(END_TIME);

            verify(timeEntryRepository).save(timeEntry);
        }

        @Test
        void patchesDescriptionAndSaves() {
            final User user = createUser(USER_ID);
            final Project project = createProject();
            final Task task = createTask();
            final TimeEntry timeEntry = new TimeEntry(user, project, task, "MyDescription", true, START_TIME, END_TIME);

            when(timeEntryRepository.findByIdAndUserId(TIME_ENTRY_ID, USER_ID)).thenReturn(Optional.of(timeEntry));
            when(timeEntryRepository.save(timeEntry)).thenReturn(timeEntry);

            final TimeEntry patchedTimeEntry = timeEntryService.patchTimeEntry(USER_ID, TIME_ENTRY_ID,
                    new PatchTimeEntryRequestBuilder().description("MyNewDescription").build());

            assertThat(patchedTimeEntry.getProject()).isEqualTo(project);
            assertThat(patchedTimeEntry.getTask()).isEqualTo(task);
            assertThat(patchedTimeEntry.getDescription()).isEqualTo("MyNewDescription");
            assertThat(patchedTimeEntry.isBillable()).isTrue();
            assertThat(patchedTimeEntry.getStartTime()).isEqualTo(START_TIME);
            assertThat(patchedTimeEntry.getEndTime()).isEqualTo(END_TIME);

            verify(timeEntryRepository).save(timeEntry);
        }

        @Test
        void patchesBillableAndSaves() {
            final User user = createUser(USER_ID);
            final Project project = createProject();
            final Task task = createTask();
            final TimeEntry timeEntry = new TimeEntry(user, project, task, "MyDescription", true, START_TIME, END_TIME);

            when(timeEntryRepository.findByIdAndUserId(TIME_ENTRY_ID, USER_ID)).thenReturn(Optional.of(timeEntry));
            when(timeEntryRepository.save(timeEntry)).thenReturn(timeEntry);

            final TimeEntry patchedTimeEntry = timeEntryService.patchTimeEntry(USER_ID, TIME_ENTRY_ID,
                    new PatchTimeEntryRequestBuilder().isBillable(false).build());

            assertThat(patchedTimeEntry.getProject()).isEqualTo(project);
            assertThat(patchedTimeEntry.getTask()).isEqualTo(task);
            assertThat(patchedTimeEntry.getDescription()).isEqualTo("MyDescription");
            assertThat(patchedTimeEntry.isBillable()).isFalse();
            assertThat(patchedTimeEntry.getStartTime()).isEqualTo(START_TIME);
            assertThat(patchedTimeEntry.getEndTime()).isEqualTo(END_TIME);

            verify(timeEntryRepository).save(timeEntry);
        }

        @Test
        void patchesStartTimeAndSaves() {
            final User user = createUser(USER_ID);
            final Project project = createProject();
            final Task task = createTask();
            final TimeEntry timeEntry = new TimeEntry(user, project, task, "MyDescription", true, START_TIME, END_TIME);
            final Instant newStartTime = START_TIME.plusSeconds(3600);

            when(timeEntryRepository.findByIdAndUserId(TIME_ENTRY_ID, USER_ID)).thenReturn(Optional.of(timeEntry));
            when(timeEntryRepository.save(timeEntry)).thenReturn(timeEntry);

            final TimeEntry patchedTimeEntry = timeEntryService.patchTimeEntry(USER_ID, TIME_ENTRY_ID,
                    new PatchTimeEntryRequestBuilder().startTime(newStartTime).build());

            assertThat(patchedTimeEntry.getProject()).isEqualTo(project);
            assertThat(patchedTimeEntry.getTask()).isEqualTo(task);
            assertThat(patchedTimeEntry.getDescription()).isEqualTo("MyDescription");
            assertThat(patchedTimeEntry.isBillable()).isTrue();
            assertThat(patchedTimeEntry.getStartTime()).isEqualTo(newStartTime);
            assertThat(patchedTimeEntry.getEndTime()).isEqualTo(END_TIME);

            verify(timeEntryRepository).save(timeEntry);
        }

        @Test
        void patchesEndTimeAndSaves() {
            final User user = createUser(USER_ID);
            final Project project = createProject();
            final Task task = createTask();
            final TimeEntry timeEntry = new TimeEntry(user, project, task, "MyDescription", true, START_TIME, END_TIME);
            final Instant newEndTime = END_TIME.plusSeconds(3600);

            when(timeEntryRepository.findByIdAndUserId(TIME_ENTRY_ID, USER_ID)).thenReturn(Optional.of(timeEntry));
            when(timeEntryRepository.save(timeEntry)).thenReturn(timeEntry);

            final TimeEntry patchedTimeEntry = timeEntryService.patchTimeEntry(USER_ID, TIME_ENTRY_ID,
                    new PatchTimeEntryRequestBuilder().endTime(newEndTime).build());

            assertThat(patchedTimeEntry.getProject()).isEqualTo(project);
            assertThat(patchedTimeEntry.getTask()).isEqualTo(task);
            assertThat(patchedTimeEntry.getDescription()).isEqualTo("MyDescription");
            assertThat(patchedTimeEntry.isBillable()).isTrue();
            assertThat(patchedTimeEntry.getStartTime()).isEqualTo(START_TIME);
            assertThat(patchedTimeEntry.getEndTime()).isEqualTo(newEndTime);

            verify(timeEntryRepository).save(timeEntry);
        }

        @Test
        void patchesNothingWhenAllFieldsUndefined() {
            final User user = createUser(USER_ID);
            final Project project = createProject();
            final Task task = createTask();
            final TimeEntry timeEntry = new TimeEntry(user, project, task, "MyDescription", true, START_TIME, END_TIME);

            when(timeEntryRepository.findByIdAndUserId(TIME_ENTRY_ID, USER_ID)).thenReturn(Optional.of(timeEntry));
            when(timeEntryRepository.save(timeEntry)).thenReturn(timeEntry);

            final TimeEntry patchedTimeEntry = timeEntryService.patchTimeEntry(USER_ID, TIME_ENTRY_ID,
                    new PatchTimeEntryRequestBuilder().build());

            assertThat(patchedTimeEntry.getProject()).isEqualTo(project);
            assertThat(patchedTimeEntry.getTask()).isEqualTo(task);
            assertThat(patchedTimeEntry.getDescription()).isEqualTo("MyDescription");
            assertThat(patchedTimeEntry.isBillable()).isTrue();
            assertThat(patchedTimeEntry.getStartTime()).isEqualTo(START_TIME);
            assertThat(patchedTimeEntry.getEndTime()).isEqualTo(END_TIME);

            verify(timeEntryRepository).save(timeEntry);
        }

        @Test
        void throwsWhenTimeEntryNotFound() {
            when(timeEntryRepository.findByIdAndUserId(TIME_ENTRY_ID, USER_ID)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> timeEntryService.patchTimeEntry(USER_ID, TIME_ENTRY_ID,
                    new PatchTimeEntryRequestBuilder().description("MyNewDescription").build()))
                    .isInstanceOf(TimeEntryNotFoundException.class)
                    .hasMessage("Time entry with ID 3 does not exist");

            verify(timeEntryRepository, never()).save(any());
        }
    }

    @Nested
    class DeleteTimeEntry {

        @Test
        void deletesTimeEntryWhenFound() {
            final TimeEntry timeEntry = createTimeEntry(TIME_ENTRY_ID);
            when(timeEntryRepository.findByIdAndUserId(TIME_ENTRY_ID, USER_ID)).thenReturn(Optional.of(timeEntry));

            timeEntryService.deleteTimeEntry(USER_ID, TIME_ENTRY_ID);

            verify(timeEntryRepository).deleteById(TIME_ENTRY_ID);
        }

        @Test
        void throwsWhenTimeEntryNotFound() {
            when(timeEntryRepository.findByIdAndUserId(TIME_ENTRY_ID, USER_ID)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> timeEntryService.deleteTimeEntry(USER_ID, TIME_ENTRY_ID))
                    .isInstanceOf(TimeEntryNotFoundException.class)
                    .hasMessage("Time entry with ID 3 does not exist");

            verify(timeEntryRepository, never()).deleteById(anyLong());
        }
    }

    private static TimeEntry createTimeEntry(long id) {
        final TimeEntry timeEntry = new TimeEntry(
                createUser(USER_ID), createProject(), null, "MyDescription", true, START_TIME, END_TIME);
        ReflectionTestUtils.setField(timeEntry, "id", id);

        return timeEntry;
    }

    private static List<DailyEntryCount> createDayGroups() {
        return List.of(
                createDayGroup(DAY_FOUR, 3),
                createDayGroup(DAY_THREE, 1),
                createDayGroup(DAY_TWO, 2),
                createDayGroup(DAY_ONE, 1));
    }

    private static DailyEntryCount createDayGroup(long epochDay, long entryCount) {
        return new TestDailyEntryCount(epochDay, entryCount);
    }

    private static Instant dayStart(long epochDay) {
        return Instant.ofEpochMilli(epochDay * MILLIS_PER_DAY);
    }

    private static final class PatchTimeEntryRequestBuilder {
        private JsonNullable<Project> project = JsonNullable.undefined();
        private JsonNullable<Task> task = JsonNullable.undefined();
        private JsonNullable<String> description = JsonNullable.undefined();
        private JsonNullable<Boolean> isBillable = JsonNullable.undefined();
        private JsonNullable<Instant> startTime = JsonNullable.undefined();
        private JsonNullable<Instant> endTime = JsonNullable.undefined();

        private PatchTimeEntryRequestBuilder project(Project project) {
            this.project = JsonNullable.of(project);
            return this;
        }

        private PatchTimeEntryRequestBuilder task(Task task) {
            this.task = JsonNullable.of(task);
            return this;
        }

        private PatchTimeEntryRequestBuilder description(String description) {
            this.description = JsonNullable.of(description);
            return this;
        }

        private PatchTimeEntryRequestBuilder isBillable(Boolean isBillable) {
            this.isBillable = JsonNullable.of(isBillable);
            return this;
        }

        private PatchTimeEntryRequestBuilder startTime(Instant startTime) {
            this.startTime = JsonNullable.of(startTime);
            return this;
        }

        private PatchTimeEntryRequestBuilder endTime(Instant endTime) {
            this.endTime = JsonNullable.of(endTime);
            return this;
        }

        private PatchTimeEntryRequest build() {
            return new PatchTimeEntryRequest(
                    project,
                    task,
                    description,
                    isBillable,
                    startTime,
                    endTime);
        }
    }

    private record TestDailyEntryCount(long epochDay, long entryCount) implements DailyEntryCount {

        @Override
        public Long getEpochDay() {
            return epochDay;
        }

        @Override
        public Long getEntryCount() {
            return entryCount;
        }
    }
}
