import { AsyncPipe } from '@angular/common';
import { ChangeDetectionStrategy, Component, inject, model } from '@angular/core';
import { map } from 'rxjs';
import { Project } from '../../project/models/project.model';
import { ProjectTaskSelectorButton } from '../../project/project-selector-button/project-task-selector-button';
import { TimerService } from '../../shared/services/timer.service';
import { durationFromMs, formatHHMMSSTime } from '../../shared/util/time.util';
import { TimeEntry } from '../models/time-entry.model';
import { TimeEntryStore } from '../stores/time-entry.store';
import { TimeEntryBillableButton } from '../time-entry-billable-button/time-entry-billable-button';
import { TimeEntryDescription } from '../time-entry-description/time-entry-description';

@Component({
  selector: 'app-active-time-entry',
  imports: [
    AsyncPipe,
    TimeEntryDescription,
    ProjectTaskSelectorButton,
    TimeEntryBillableButton],
  templateUrl: './active-time-entry.html',
  changeDetection: ChangeDetectionStrategy.Eager,
  styleUrl: './active-time-entry.css',
})
export class ActiveTimeEntry {
  private readonly timeEntryStore: TimeEntryStore = inject(TimeEntryStore);
  private readonly timerService: TimerService = inject(TimerService);

  readonly activeTimeEntry = model.required<TimeEntry>();

  elapsedTime$ = this.timerService.oneSecondTick$.pipe(map(() => this.formatElapsedTime()));

  formatElapsedTime(): string {
    const start: number = new Date(this.activeTimeEntry().startTime).getTime();
    const diffMs: number = Math.max(0, Date.now() - start); // clamp negatives (future dates)

    return formatHHMMSSTime(durationFromMs(diffMs));
  }

  saveTimeEntryDescription(newDescription: string): void {
    this.timeEntryStore.patchDescription(this.activeTimeEntry(), newDescription);
  }

  stopTimeEntry(): void {
    this.timeEntryStore.stopActive(this.activeTimeEntry());
  }

  deleteTimeEntry(): void {
    this.timeEntryStore.delete(this.activeTimeEntry());
  }

  updateTimeEntryProject(project: Project): void {
    this.timeEntryStore.patchProject(this.activeTimeEntry(), project);
  }
}
