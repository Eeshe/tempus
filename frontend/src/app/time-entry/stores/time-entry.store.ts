import { formatDate } from "@angular/common";
import { computed, inject, Service, Signal, signal } from "@angular/core";
import { Project } from "../../project/models/project.model";
import { computeDuration, Duration, durationFromMs, formatHHMMSSTime } from "../../shared/util/time.util";
import { Task } from "../../task/models/task.model";
import { createEmptyTimeEntryPage, TimeEntryPage } from "../models/time-entry-page.model";
import { TimeEntry } from "../models/time-entry.model";
import { TimeEntryService } from "../services/time-entry.service";

export interface TimeEntryGroupItem {
  id: string;
  entries: TimeEntry[];
}

export interface DayGroupedTimeEntries {
  // Page-scoped identity. Two pages can share the same local dayKey (backend paginates by UTC day,
  // the store groups by local day), so the key must also include the page cursor to stop Angular
  // from reusing a day view across a page change and skipping its enter animation.
  id: string;
  dayKey: string;
  formattedDate: string;
  allEntries: Map<string, TimeEntryGroupItem>; // All time entries, including active ones
  endedEntries: Map<string, TimeEntry[]>; // Only ended time entries
  formattedTotalTime: string;
}

@Service()
export class TimeEntryStore {
  private readonly timeEntryService: TimeEntryService = inject(TimeEntryService);

  private readonly _timeEntryPage = signal<TimeEntryPage>(createEmptyTimeEntryPage());
  private readonly _lastStoppedTimeEntry = signal<TimeEntry | null>(null);

  // Persistent identity of time entry groups. The cache key is "dayKey/groupKey", the value is the
  // synthetic group id. Ids survive individual time entry changes so Angular can track groups stably.
  private readonly groupIdByKey = new Map<string, string>();
  // Composition (entry ids) of the groups produced by the previous recompute, used to carry an id
  // over to a group whose key changed (e.g. bulk edit) but whose entries are still the same.
  private previousGroupIds = new Map<string, Set<number>>();
  private groupIdCounter: number = 0;

  readonly timeEntryPage: Signal<TimeEntryPage> = this._timeEntryPage.asReadonly();
  readonly lastStoppedTimeEntry: Signal<TimeEntry | null> = this._lastStoppedTimeEntry.asReadonly();
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
    const pageCursor: string = this._timeEntryPage().currentCursor;
    const dayGroups: Map<string, Map<string, TimeEntry[]>> = new Map<string, Map<string, TimeEntry[]>>();

    for (const timeEntry of this.timeEntries()) {
      const dayKey: string = this.formatDayKey(timeEntry.startTime);
      const timeEntryGroupId: string = JSON.stringify([
        timeEntry.project.id,
        timeEntry.description ?? null,
        timeEntry.task?.id ?? null,
        timeEntry.isBillable,
      ]);

      const dayProjectGroups: Map<string, TimeEntry[]> =
        dayGroups.get(dayKey) ?? new Map<string, TimeEntry[]>();
      const groupedEntries: TimeEntry[] = dayProjectGroups.get(timeEntryGroupId) ?? [];

      groupedEntries.push(timeEntry);
      dayProjectGroups.set(timeEntryGroupId, groupedEntries);
      dayGroups.set(dayKey, dayProjectGroups);
    }
    const sortedDayGroups: Map<string, Map<string, TimeEntry[]>> = new Map(
      Array.from(dayGroups, ([dayKey, groupedEntries]) => [
        dayKey,
        // Sort the Map entries by the first entry's startTime
        new Map<string, TimeEntry[]>(
          Array.from(groupedEntries)
            .sort(([, entriesA], [, entriesB]) => {
              return entriesA[entriesA.length - 1].startTime.localeCompare(entriesB[entriesB.length - 1].startTime) ?? 0;
            })
            .reverse()
        ),
      ] as const)
    );
    const groupIdsByDayKey: Map<string, Map<string, string>> = this.computeGroupIds(sortedDayGroups);

    return Array.from(sortedDayGroups, ([dayKey, sortedGroupedEntries]) => {
      const dayGroupIds: Map<string, string> = groupIdsByDayKey.get(dayKey)!;
      const allEntries = new Map<string, TimeEntryGroupItem>(
        Array.from(sortedGroupedEntries, ([groupKey, entries]) => [
          groupKey,
          { id: dayGroupIds.get(groupKey)!, entries: entries },
        ] as const)
      );
      const endedGroupedEntries = new Map(
        Array.from(sortedGroupedEntries, ([key, entries]) => [
          key,
          entries.filter(e => e.endTime !== null)
        ] as const)
      );
      const totalTimeMs: number = [...sortedGroupedEntries.values()]
        .flatMap((entries) => entries)
        .reduce((sum, timeEntry) => {
          const duration: Duration | null = computeDuration(timeEntry.startTime, timeEntry.endTime);

          return sum + (duration?.totalMilliseconds ?? 0);
        }, 0);
      const formattedTotalTime: string = formatHHMMSSTime(durationFromMs(totalTimeMs));
      const firstEntry: TimeEntry = sortedGroupedEntries.values().next().value![0];
      return {
        id: `${pageCursor}\u0000${dayKey}`,
        dayKey: dayKey,
        formattedDate: this.formatDayDate(firstEntry.startTime),
        allEntries: allEntries,
        endedEntries: endedGroupedEntries,
        formattedTotalTime: formattedTotalTime,
      };
    });
  }

  /**
   * Assigns a stable synthetic id to every time entry group. Ids are independent of the time entries
   * they contain, so editing, moving or deleting an entry no longer changes a group's track key.
   */
  private computeGroupIds(dayGroups: Map<string, Map<string, TimeEntry[]>>): Map<string, Map<string, string>> {
    const groupIdsByDayKey: Map<string, Map<string, string>> = new Map<string, Map<string, string>>();
    const claimedIds: Set<string> = new Set<string>();

    // Pass 1: reuse the id already associated with a group key.
    for (const [dayKey, groupedEntries] of dayGroups) {
      const dayGroupIds: Map<string, string> = new Map<string, string>();
      for (const groupKey of groupedEntries.keys()) {
        const cachedId: string | undefined = this.groupIdByKey.get(this.groupCacheKey(dayKey, groupKey));
        if (cachedId != null) {
          dayGroupIds.set(groupKey, cachedId);
          claimedIds.add(cachedId);
        }
      }
      groupIdsByDayKey.set(dayKey, dayGroupIds);
    }

    // Pass 2: a group whose key changed (e.g. bulk edit) adopts the id of a vanished group when
    // their entries overlap. Otherwise it gets a fresh id.
    for (const [dayKey, groupedEntries] of dayGroups) {
      const dayGroupIds: Map<string, string> = groupIdsByDayKey.get(dayKey)!;
      for (const [groupKey, entries] of groupedEntries) {
        if (dayGroupIds.has(groupKey)) {
          continue;
        }
        const newEntryIds: Set<number> = new Set(entries.map(timeEntry => timeEntry.id));
        const carriedOverId: string | null = this.findCarriedOverId(newEntryIds, claimedIds);
        const groupId: string = carriedOverId ?? `time-entry-group-${++this.groupIdCounter}`;

        dayGroupIds.set(groupKey, groupId);
        claimedIds.add(groupId);
        this.groupIdByKey.set(this.groupCacheKey(dayKey, groupKey), groupId);
      }
    }

    // Remember the current composition for the next recompute.
    const currentGroupIds: Map<string, Set<number>> = new Map<string, Set<number>>();
    for (const [dayKey, groupedEntries] of dayGroups) {
      const dayGroupIds: Map<string, string> = groupIdsByDayKey.get(dayKey)!;
      for (const [groupKey, entries] of groupedEntries) {
        currentGroupIds.set(dayGroupIds.get(groupKey)!, new Set(entries.map(timeEntry => timeEntry.id)));
      }
    }
    this.previousGroupIds = currentGroupIds;

    return groupIdsByDayKey;
  }

  private findCarriedOverId(newEntryIds: Set<number>, claimedIds: Set<string>): string | null {
    let bestId: string | null = null;
    let bestOverlap: number = 0;

    for (const [previousId, previousEntryIds] of this.previousGroupIds) {
      if (claimedIds.has(previousId)) {
        continue;
      }
      let overlap: number = 0;
      for (const entryId of newEntryIds) {
        if (previousEntryIds.has(entryId)) {
          overlap++;
        }
      }
      if (overlap > bestOverlap) {
        bestOverlap = overlap;
        bestId = previousId;
      }
    }

    return bestId;
  }

  private groupCacheKey(dayKey: string, groupKey: string): string {
    return `${dayKey}\u0000${groupKey}`;
  }

  private formatDayDate(date: string | null) {
    return date ? formatDate(date, 'EEEE, MMM d', 'en-US') : 'Unknown';
  }

  private formatDayKey(date: string | null) {
    return date ? formatDate(date, 'yyyy-MM-dd', 'en-US') : 'unknown';
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

  resumeLastStopped(): void {
    const lastStoppedTimeEntry: TimeEntry | null = this._lastStoppedTimeEntry();
    if (lastStoppedTimeEntry == null) {
      return;
    }
    this._lastStoppedTimeEntry.set(null);
    this.resume(lastStoppedTimeEntry);
  }

  add(timeEntry: TimeEntry): void {
    this.loadPage(this._timeEntryPage().currentCursor);
  }

  stopActive(timeEntry: TimeEntry): void {
    this._lastStoppedTimeEntry.set(timeEntry);
    this.patchEndTime(timeEntry, new Date());
  }

  delete(timeEntry: TimeEntry): void {
    this.timeEntryService.deleteTimeEntry(timeEntry).subscribe(() =>
      this._timeEntryPage.update(timeEntryPage => ({
        ...timeEntryPage,
        content: timeEntryPage.content.filter(previousTimeEntry => previousTimeEntry.id !== timeEntry.id),
      })));
  }

  deleteMany(timeEntries: TimeEntry[]): void {
    const deletedIds: Set<number> = new Set(timeEntries.map(timeEntry => timeEntry.id));
    this.timeEntryService.deleteTimeEntries(timeEntries).subscribe(() =>
      this._timeEntryPage.update(timeEntryPage => ({
        ...timeEntryPage,
        content: timeEntryPage.content.filter(previousTimeEntry => !deletedIds.has(previousTimeEntry.id)),
      })));
  }

  patchDescription(timeEntry: TimeEntry, newDescription: string): void {
    this.timeEntryService.patchTimeEntryDescription(timeEntry, newDescription)
      .subscribe((patchedTimeEntry) => this.replace(patchedTimeEntry));
  }

  patchDescriptions(timeEntries: TimeEntry[], newDescription: string): void {
    this.timeEntryService.patchTimeEntriesDescription(timeEntries, newDescription)
      .subscribe((patchedTimeEntries) => this.replaceAll(patchedTimeEntries));
  }

  patchProject(timeEntry: TimeEntry, newProject: Project): void {
    this.timeEntryService.patchTimeEntryProject(timeEntry, newProject)
      .subscribe((patchedTimeEntry) => this.replace(patchedTimeEntry));
  }

  patchProjects(timeEntries: TimeEntry[], newProject: Project): void {
    this.timeEntryService.patchTimeEntriesProject(timeEntries, newProject)
      .subscribe((patchedTimeEntries) => this.replaceAll(patchedTimeEntries));
  }

  patchTask(timeEntry: TimeEntry, newProject: Project, newTask: Task): void {
    this.timeEntryService.patchTimeEntryTask(timeEntry, newProject, newTask)
      .subscribe((patchedTimeEntry) => this.replace(patchedTimeEntry));
  }

  patchTasks(timeEntries: TimeEntry[], newProject: Project, newTask: Task): void {
    this.timeEntryService.patchTimeEntriesTask(timeEntries, newProject, newTask)
      .subscribe((patchedTimeEntries) => this.replaceAll(patchedTimeEntries));
  }

  patchBillable(timeEntry: TimeEntry, billable: boolean): void {
    this.timeEntryService.patchTimeEntryBillable(timeEntry, billable)
      .subscribe((patchedTimeEntry) => this.replace(patchedTimeEntry));
  }

  patchBillables(timeEntries: TimeEntry[], billable: boolean): void {
    this.timeEntryService.patchTimeEntriesBillable(timeEntries, billable)
      .subscribe((patchedTimeEntries) => this.replaceAll(patchedTimeEntries));
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

  private replaceAll(updatedTimeEntries: TimeEntry[]): void {
    const updatedById: Map<number, TimeEntry> = new Map(
      updatedTimeEntries.map(timeEntry => [timeEntry.id, timeEntry])
    );
    this._timeEntryPage.update((timeEntryPage) => ({
      ...timeEntryPage,
      content: timeEntryPage.content.map(timeEntry => updatedById.get(timeEntry.id) ?? timeEntry),
    }));
  }
}
