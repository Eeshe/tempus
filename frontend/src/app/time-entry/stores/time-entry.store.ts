import { formatDate } from "@angular/common";
import { computed, inject, Service, Signal, signal } from "@angular/core";
import { Project } from "../../project/models/project.model";
import { computeDuration, Duration, durationFromMs, formatHHMMSSTime } from "../../shared/util/time.util";
import { Task } from "../../task/models/task.model";
import { createEmptyTimeEntryPage, TimeEntryPage } from "../models/time-entry-page.model";
import { TimeEntry } from "../models/time-entry.model";
import { TimeEntryService } from "../services/time-entry.service";


export interface DayGroupedTimeEntries {
  formattedDate: string;
  allEntries: Map<string, TimeEntry[]>; // All time entries, including active ones
  endedEntries: Map<string, TimeEntry[]>; // Only ended time entries
  formattedTotalTime: string;
}

@Service()
export class TimeEntryStore {
  private readonly timeEntryService: TimeEntryService = inject(TimeEntryService);

  private readonly _timeEntryPage = signal<TimeEntryPage>(createEmptyTimeEntryPage());

  readonly timeEntryPage: Signal<TimeEntryPage> = this._timeEntryPage.asReadonly();
  readonly timeEntries: Signal<TimeEntry[]> = computed(() => {
    return this.timeEntryPage().content.sort((timeEntryA, timeEntryB) =>
      timeEntryA.startTime.localeCompare(timeEntryB.startTime, undefined)).reverse()
  });

  readonly activeTimeEntries = computed<TimeEntry[]>(() => {
    if (this.timeEntryPage == null) {
      return [];
    }
    return this.timeEntries().filter(timeEntry => timeEntry.endTime === null);
  });

  readonly dayGroupedTimeEntries = computed<DayGroupedTimeEntries[]>(() => this.groupTimeEntriesByDay());

  groupTimeEntriesByDay(): DayGroupedTimeEntries[] {
    const dayGroups: Map<string, Map<string, TimeEntry[]>> = new Map<string, Map<string, TimeEntry[]>>();

    for (const timeEntry of this.timeEntries()) {
      const formattedDate: string = this.formatDayDate(timeEntry.startTime);
      // ID is project id + description + taskId + billable
      // Ex. Tempusv0.9.0Developmentfalse
      const timeEntryGroupId: string =
        timeEntry.project.id +
        (timeEntry.description ? timeEntry.description : '') +
        (timeEntry.task ? timeEntry.task.id : '') +
        timeEntry.isBillable;

      const dayProjectGroups: Map<string, TimeEntry[]> =
        dayGroups.get(formattedDate) ?? new Map<string, TimeEntry[]>();
      const groupedEntries: TimeEntry[] = dayProjectGroups.get(timeEntryGroupId) ?? [];

      groupedEntries.push(timeEntry);
      dayProjectGroups.set(timeEntryGroupId, groupedEntries);
      dayGroups.set(formattedDate, dayProjectGroups);
    }
    return Array.from(dayGroups, ([formattedDate, groupedEntries]) => {
      // Sort the Map entries by the first entry's startTime
      const sortedGroupedEntries = new Map<string, TimeEntry[]>(
        Array.from(groupedEntries)
          .sort(([, entriesA], [, entriesB]) => {
            return entriesA[entriesA.length - 1].startTime.localeCompare(entriesB[entriesB.length - 1].startTime) ?? 0;
          })
          .reverse()
      );
      const endedGroupedEntries = new Map(
        Array.from(sortedGroupedEntries, ([key, entries]) => [
          key,
          entries.filter(e => e.endTime !== null)
        ] as const)
      );
      const totalTimeMs: number = [...groupedEntries.values()]
        .flatMap((entries) => entries)
        .reduce((sum, timeEntry) => {
          const duration: Duration | null = computeDuration(timeEntry.startTime, timeEntry.endTime);

          return sum + (duration?.totalMilliseconds ?? 0);
        }, 0);
      const formattedTotalTime: string = formatHHMMSSTime(durationFromMs(totalTimeMs));
      return {
        formattedDate: formattedDate,
        allEntries: sortedGroupedEntries,
        endedEntries: endedGroupedEntries,
        formattedTotalTime: formattedTotalTime,
      };
    });
  }

  private formatDayDate(date: string | null) {
    return date ? formatDate(date, 'EEEE, MMM d', 'en-US') : 'Unknown';
  }

  loadPage(cursor: string | null = null): void {
    this.timeEntryService.listTimeEntries(cursor).subscribe(timeEntryPage => {
      this._timeEntryPage.set(timeEntryPage);
    })
  }

  resume(timeEntry: TimeEntry): void {
    this.timeEntryService.resumeTimeEntry(timeEntry).subscribe(resumedTimeEntry => {
      this.add(resumedTimeEntry);
    })
  }

  add(timeEntry: TimeEntry): void {
    this.loadPage(this._timeEntryPage().currentCursor);
  }

  stopActive(timeEntry: TimeEntry): void {
    this.patchEndTime(timeEntry, new Date());
  }

  delete(timeEntry: TimeEntry): void {
    this.timeEntryService.deleteTimeEntry(timeEntry).subscribe(() =>
      this._timeEntryPage.update(timeEntryPage => ({
        ...timeEntryPage,
        content: timeEntryPage.content.filter(previousTimeEntry => previousTimeEntry.id !== timeEntry.id),
      })));
  }

  patchDescription(timeEntry: TimeEntry, newDescription: string): void {
    this.timeEntryService.patchTimeEntryDescription(timeEntry, newDescription)
      .subscribe((patchedTimeEntry) => this.replace(patchedTimeEntry));
  }

  patchProject(timeEntry: TimeEntry, newProject: Project): void {
    this.timeEntryService.patchTimeEntryProject(timeEntry, newProject)
      .subscribe((patchedTimeEntry) => this.replace(patchedTimeEntry));
  }

  patchTask(timeEntry: TimeEntry, newProject: Project, newTask: Task): void {
    this.timeEntryService.patchTimeEntryTask(timeEntry, newProject, newTask)
      .subscribe((patchedTimeEntry) => this.replace(patchedTimeEntry));
  }

  patchBillable(timeEntry: TimeEntry, billable: boolean): void {
    this.timeEntryService.patchTimeEntryBillable(timeEntry, billable)
      .subscribe((patchedTimeEntry) => this.replace(patchedTimeEntry));
  }

  patchStartTime(timeEntry: TimeEntry, newStartTime: Date): void {
    this.timeEntryService.patchTimeEntryStartTime(timeEntry, newStartTime)
      .subscribe((patchedTimeEntry) => this.replace(patchedTimeEntry));
  }

  patchEndTime(timeEntry: TimeEntry, newEndTime: Date): void {
    this.timeEntryService.patchTimeEntryEndTime(timeEntry, newEndTime)
      .subscribe((patchedTimeEntry) => this.replace(patchedTimeEntry));
  }

  private replace(updatedTimeEntry: TimeEntry): void {
    this._timeEntryPage.update((timeEntryPage) =>
    ({
      ...timeEntryPage,
      content: timeEntryPage.content.map(timeEntry =>
        timeEntry.id === updatedTimeEntry.id ?
          updatedTimeEntry : timeEntry)
    })
    );
  }
}
