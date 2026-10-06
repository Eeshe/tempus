import { Component, inject, signal } from '@angular/core';
import { form, FormField, required, submit, validate } from '@angular/forms/signals';
import { firstValueFrom } from 'rxjs';
import { Project } from '../../../project/models/project.model';
import { ProjectTaskSelectorButton } from '../../../project/project-selector-button/project-task-selector-button';
import { DateInput } from '../../../shared/input/date-input/date-input';
import { TimeInput } from '../../../shared/input/time-input/time-input';
import { AppModal } from '../../../shared/modal/modal';
import { ModalBase } from '../../../shared/modal/modal-base';
import { toHHmmTime } from '../../../shared/util/time.util';
import { Task } from '../../../task/models/task.model';
import { TimeEntry } from '../../models/time-entry.model';
import { TimeEntryService } from '../../services/time-entry.service';
import { TimeEntryStore } from '../../stores/time-entry.store';

interface NewTimeEntryModel {
  project: Project | null;
  description: string;
  task: Task | null;
  billable: boolean;
  startDate: string;
  startTime: string;
  endDate: string | null;
  endTime: string | null;
}

function toLocalDateValue(date: Date): string {
  const pad = (n: number) => n.toString().padStart(2, '0');
  return `${date.getFullYear()}-${pad(date.getMonth() + 1)}-${pad(date.getDate())}`;
}

function combineToISO(date: string, time: string): string {
  return new Date(`${date}T${time}`).toISOString();
}

function isValidTime(time: string | null): boolean {
  return time !== null && /^\d{2}:\d{2}$/.test(time);
}

function isValidDate(date: string | null): boolean {
  if (date === null || !/^\d{4}-\d{2}-\d{2}$/.test(date)) {
    return false;
  }
  const parsed = new Date(date);
  return !isNaN(parsed.getTime()) && date === parsed.toISOString().slice(0, 10);
}

@Component({
  imports: [FormField, ProjectTaskSelectorButton, TimeInput, DateInput, AppModal],
  selector: 'app-new-time-entry-form-modal',
  styleUrl: './new-time-entry-form-modal.css',
  templateUrl: './new-time-entry-form-modal.html',
})
export class NewTimeEntryFormModal extends ModalBase {
  private readonly timeEntryService: TimeEntryService = inject(TimeEntryService);
  private readonly timeEntryStore: TimeEntryStore = inject(TimeEntryStore);

  readonly newTimeEntryModel = signal<NewTimeEntryModel>({
    project: null,
    description: '',
    task: null,
    billable: true,
    startDate: toLocalDateValue(new Date()),
    startTime: toHHmmTime(new Date()),
    endDate: null,
    endTime: null,
  });

  readonly newTimeEntryForm = form(this.newTimeEntryModel, (fieldPath) => {
    required(fieldPath.project, { message: 'Project is required' });
    required(fieldPath.startDate, { message: 'Start date is required' });
    required(fieldPath.startTime, { message: 'Start time is required' });
    validate(fieldPath.startDate, (ctx) =>
      isValidDate(ctx.value()) ? null : { kind: 'dateFormat', message: 'Enter a valid date (YYYY-MM-DD)' });
    validate(fieldPath.endDate, (ctx) =>
      ctx.value() === null || isValidDate(ctx.value()) ? null : { kind: 'dateFormat', message: 'Enter a valid date (YYYY-MM-DD)' });
    validate(fieldPath.startTime, (ctx) =>
      isValidTime(ctx.value()) ? null : { kind: 'timeFormat', message: 'Enter a valid time (HHmm)' });
    validate(fieldPath.endTime, (ctx) =>
      ctx.value() === null || isValidTime(ctx.value()) ? null : { kind: 'timeFormat', message: 'Enter a valid time (HHmm)' });
  });

  assignProject(project: Project): void {
    this.newTimeEntryModel.update(model => ({ ...model, project, task: null, }));
  }

  assignTask(project: Project, task: Task | null): void {
    this.newTimeEntryModel.update(model => ({ ...model, project, task }));
  }

  onSubmit(event: Event): void {
    event.preventDefault();

    submit(this.newTimeEntryForm, async () => {
      try {
        const model = this.newTimeEntryModel();
        const createdTimeEntry: TimeEntry = await firstValueFrom(this.timeEntryService.createTimeEntry(
          model.project!.id,
          null,
          model.description,
          model.billable,
          combineToISO(model.startDate, model.startTime),
          model.endTime ? combineToISO(model.endDate ?? model.startDate, model.endTime) : null));

        this.timeEntryStore.add(createdTimeEntry);
        this.close();
        return null;
      } catch (error) {
        return { kind: 'serverError', message: "Something's wrong server-side" };
      }
    });
  }
}
