import { Component, inject, input, linkedSignal, output, signal } from '@angular/core';
import { RouterLink } from '@angular/router';
import { ClientSelectorButton } from '../../client/client-selector-button/client-selector-button';
import { Client } from '../../client/models/client.model';
import { DeleteConfirmationModal } from '../../shared/delete-confirmation-modal/delete-confirmation-modal';
import { InlineEditInput } from '../../shared/input/inline-edit-input/inline-edit-input';
import { AppModal } from '../../shared/modal/modal';
import { ModalBase } from '../../shared/modal/modal-base';
import { Task } from '../../task/models/task.model';
import { TaskList } from '../../task/task-list/task-list';
import { Project } from '../models/project.model';
import { ProjectReportStore } from '../store/project-report.store';

@Component({
  imports: [
    AppModal,
    RouterLink,
    InlineEditInput,
    ClientSelectorButton,
    TaskList,
    DeleteConfirmationModal
  ],
  selector: 'app-project-modal',
  styleUrl: './project-modal.css',
  templateUrl: './project-modal.html',
})
export class ProjectModal extends ModalBase {
  private readonly projectReportStore: ProjectReportStore = inject(ProjectReportStore);

  readonly project = input.required<Project>();

  readonly projectDeleteEvent = output<void>();

  readonly isEditingName = signal<boolean>(false);
  readonly isDeleteConfirmationModalOpen = signal<boolean>(false);
  readonly archivedInfoTooltip =
    'Archiving a project removes it from the project selection when creating a new time entry. ' +
    'You can still see previous time entries of that project both in the home and reports page';
  readonly hourlyRateDraft = linkedSignal<string>(() =>
    this.project().hourlyRate?.toString() ?? '');
  readonly hourlyRateError = signal<string | null>(null);

  startProjectNameEdit(): void {
    this.isEditingName.set(true);
  }

  editProjectName(newName: string): void {
    this.projectReportStore.editProjectName(this.project(), newName);
    this.stopProjectNameEdit();
  }

  stopProjectNameEdit(): void {
    this.isEditingName.set(false);
  }

  editProjectClient(newClient: Client | null): void {
    this.projectReportStore.editProjectClient(this.project(), newClient);
  }

  toggleProjectArchived(event: Event): void {
    const isArchived = (event.target as HTMLInputElement).checked;
    this.projectReportStore.editProjectArchived(this.project(), isArchived);
  }

  updateHourlyRateDraft(event: Event): void {
    this.hourlyRateDraft.set((event.target as HTMLInputElement).value);
  }

  commitHourlyRate(): void {
    const raw = this.hourlyRateDraft().trim();
    if (raw === '') {
      this.hourlyRateError.set(null);
      this.hourlyRateDraft.set(this.project().hourlyRate?.toString() ?? '');
      return;
    }
    const parsed = Number(raw);
    if (!Number.isFinite(parsed) || parsed < 0) {
      this.hourlyRateError.set('Hourly rate cannot be negative');
      return;
    }
    this.hourlyRateError.set(null);
    if (parsed === this.project().hourlyRate) {
      return;
    }
    this.projectReportStore.editProjectHourlyRate(this.project(), parsed);
  }

  acceptChanges(event: Event): void {
    event.preventDefault();
    (event.target as HTMLInputElement).blur();
  }

  discardChanges(event: Event): void {
    event.preventDefault();
    this.hourlyRateDraft.set(this.project().hourlyRate?.toString() ?? '');
    this.hourlyRateError.set(null);
    (event.target as HTMLInputElement).blur();
  }

  editProjectTask(task: Task): void {
    this.projectReportStore.editProjectTask(this.project(), task);
  }

  deleteProjectTask(task: Task): void {
    this.projectReportStore.deleteProjectTask(this.project(), task);
  }

  toggleDeleteConfirmationModal(): void {
    this.isDeleteConfirmationModalOpen.update(value => !value);
  }

  handleDeleteConfirmation(confirmedDeletion: boolean): void {
    if (confirmedDeletion) {
      this.projectReportStore.delete(this.project());
      this.projectDeleteEvent.emit();
    }
    this.toggleDeleteConfirmationModal();
  }
}
