import { ChangeDetectionStrategy, Component, inject, input, linkedSignal } from '@angular/core';
import { Project } from '../../project/models/project.model';
import { ProjectTaskSelectorButton } from '../../project/project-selector-button/project-task-selector-button';
import { TimeInput } from '../../shared/input/time-input/time-input';
import { DurationPipe } from '../../shared/pipes/duration.pipe';
import { fromHHmmTime, HHmmToMinutes, toHHmmTime } from '../../shared/util/time.util';
import { Task } from '../../task/models/task.model';
import { TimeEntry } from '../models/time-entry.model';
import { TimeEntryStore } from '../stores/time-entry.store';
import { TimeEntryBillableButton } from '../time-entry-billable-button/time-entry-billable-button';
import { TimeEntryDescription } from '../time-entry-description/time-entry-description';

@Component({
  selector: 'app-resumable-time-entry',
  imports: [
    DurationPipe,
    TimeEntryDescription,
    ProjectTaskSelectorButton,
    TimeEntryBillableButton,
    TimeInput,
  ],
  templateUrl: './resumable-time-entry.html',
  changeDetection: ChangeDetectionStrategy.Eager,
  styleUrl: './resumable-time-entry.css',
})
export class ResumableTimeEntry {
  private readonly timeEntryStore: TimeEntryStore = inject(TimeEntryStore);
  readonly timeEntry = input.required<TimeEntry>();

  readonly startTimeInput = linkedSignal(() =>
    toHHmmTime(new Date(this.timeEntry().startTime)),
  );
  readonly endTimeInput = linkedSignal(() =>
    toHHmmTime(new Date(this.timeEntry().endTime!))
  );

  updateTimeEntryDescription(newDescription: string): void {
    this.timeEntryStore.patchDescription(this.timeEntry(), newDescription);
  }

  updateTimeEntryProject(newProject: Project): void {
    this.timeEntryStore.patchProject(this.timeEntry(), newProject);
  }

  updateTimeEntryTask(newProject: Project, newTask: Task): void {
    this.timeEntryStore.patchTask(this.timeEntry(), newProject, newTask);
  }

  updateTimeEntryStartTime(newStartTime: string | null): void {
    if (newStartTime == null || newStartTime === "" || newStartTime.length < 5) {
      this.setDefaultStartTime();
      return;
    }
    const startTimeMinutes: number = HHmmToMinutes(newStartTime);
    const endTimeMinutes: number = HHmmToMinutes(this.endTimeInput());
    if (startTimeMinutes === endTimeMinutes) {
      this.setDefaultStartTime();
      return;
    }
    const startTimeDate: Date = new Date(this.timeEntry().startTime);
    if (startTimeMinutes > endTimeMinutes) {
      startTimeDate.setDate(startTimeDate.getDate() - 1);
    }
    const newStartTimeDate: Date = fromHHmmTime(newStartTime, startTimeDate);
    this.timeEntryStore.patchStartTime(this.timeEntry(), newStartTimeDate);
  }

  private setDefaultStartTime(): void {
    this.startTimeInput.set(toHHmmTime(new Date(this.timeEntry().startTime)));
  }

  updateTimeEntryEndTime(newEndTime: string | null): void {
    if (newEndTime == null || newEndTime === "" || newEndTime.length < 5) {
      this.setDefaultEndTime();
      return;
    }
    const startTimeMinutes: number = HHmmToMinutes(this.startTimeInput());
    const endTimeMinutes: number = HHmmToMinutes(newEndTime);
    if (startTimeMinutes === endTimeMinutes) {
      this.setDefaultEndTime();
      return;
    }
    const endTimeDate: Date = new Date(this.timeEntry().endTime!);
    if (endTimeMinutes < startTimeMinutes) {
      endTimeDate.setDate(endTimeDate.getDate() + 1);
    }
    const newEndTimeDate: Date = fromHHmmTime(newEndTime, endTimeDate);
    this.timeEntryStore.patchEndTime(this.timeEntry(), newEndTimeDate);
  }

  private setDefaultEndTime(): void {
    this.endTimeInput.set(toHHmmTime(new Date(this.timeEntry().endTime!)));
  }

  resumeTimeEntry(): void {
    this.timeEntryStore.resume(this.timeEntry());
  }

  deleteTimeEntry(): void {
    this.timeEntryStore.delete(this.timeEntry());
  }
}
