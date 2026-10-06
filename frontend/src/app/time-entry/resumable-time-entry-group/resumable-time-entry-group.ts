import { AsyncPipe } from '@angular/common';
import { ChangeDetectionStrategy, Component, computed, inject, input, signal } from '@angular/core';
import { map } from 'rxjs';
import { Project } from '../../project/models/project.model';
import { ProjectTaskSelectorButton } from '../../project/project-selector-button/project-task-selector-button';
import { TimerService } from '../../shared/services/timer.service';
import {
  computeDuration,
  Duration,
  durationFromMs,
  formatHHMMSSTime,
  toHHmmTime,
} from '../../shared/util/time.util';
import { Task } from '../../task/models/task.model';
import { TimeEntry } from '../models/time-entry.model';
import { ResumableTimeEntry } from '../resumable-time-entry/resumable-time-entry';
import { TimeEntryStore } from '../stores/time-entry.store';
import { TimeEntryDescription } from '../time-entry-description/time-entry-description';

@Component({
  selector: 'app-resumable-time-entry-group',
  imports: [
    TimeEntryDescription,
    ProjectTaskSelectorButton,
    ResumableTimeEntry,
    AsyncPipe,
  ],
  templateUrl: './resumable-time-entry-group.html',
  changeDetection: ChangeDetectionStrategy.Eager,
  styleUrl: './resumable-time-entry-group.css',
})
export class ResumableTimeEntryGroup {
  private readonly timeEntryStore: TimeEntryStore = inject(TimeEntryStore);
  private readonly timerService: TimerService = inject(TimerService);

  readonly timeEntries = input.required<TimeEntry[]>();
  readonly endedTimeEntries = computed<TimeEntry[]>(() => {
    return this.timeEntries().filter(timeEntry => timeEntry.endTime !== null);
  });
  readonly hasActiveTimeEntry = computed<boolean>(() => {
    return this.timeEntries().length != this.endedTimeEntries().length;
  });
  readonly formattedStartToEndTime = computed<string>(() => {
    const startTime: string = toHHmmTime(new Date(this.timeEntries()[this.timeEntries().length - 1].startTime));
    const endTime: string = toHHmmTime(new Date(this.endedTimeEntries()[0].endTime!));

    return startTime + " – " + endTime;
  });
  readonly formattedTotalDuration$ = this.timerService.oneSecondTick$.pipe(map(() => {
    let totalDurationMs: number = 0;
    for (const timeEntry of this.timeEntries()) {
      const duration: Duration | null = computeDuration(timeEntry.startTime, timeEntry.endTime);
      if (duration == null) {
        continue;
      }
      totalDurationMs += duration.totalMilliseconds;
    }
    return formatHHMMSSTime(durationFromMs(totalDurationMs));
  }))
  readonly isExpanded = signal<boolean>(false);

  toggleCollapsible(event?: MouseEvent): void {
    if (this.timeEntries().length <= 1) {
      return;
    }
    if (event && (event.target as HTMLElement).closest('button, input, select, textarea, a, [contenteditable="true"]')) {
      return;
    }
    this.isExpanded.update((value) => !value);
  }

  resumeTimeEntry(timeEntry: TimeEntry): void {
    this.timeEntryStore.resume(timeEntry);
  }

  deleteGroup(): void {
    this.endedTimeEntries().forEach((timeEntry) => this.timeEntryStore.delete(timeEntry));
  }

  updateGroupDescription(newDescription: string): void {
    this.timeEntries().forEach((timeEntry) => this.timeEntryStore.patchDescription(timeEntry, newDescription));
  }

  updateGroupProject(newProject: Project): void {
    this.timeEntries().forEach((timeEntry) => this.timeEntryStore.patchProject(timeEntry, newProject));
  }

  updateGroupTask(newProject: Project, newTask: Task): void {
    this.timeEntries().forEach((timeEntry) => this.timeEntryStore.patchTask(timeEntry, newProject, newTask));
  }

  updateGroupBillable(isBillable: boolean): void {
    this.timeEntries().forEach((timeEntry) => this.timeEntryStore.patchBillable(timeEntry, isBillable));
  }
}
