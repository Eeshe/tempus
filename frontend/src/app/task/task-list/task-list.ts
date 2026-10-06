import { Component, input, output, signal } from '@angular/core';
import { DeleteConfirmationModal } from '../../shared/delete-confirmation-modal/delete-confirmation-modal';
import { InlineEditInput } from '../../shared/input/inline-edit-input/inline-edit-input';
import { SortableColumn, SortableTable } from '../../shared/sortable-table/sortable-table';
import { Task } from '../models/task.model';

@Component({
  imports: [SortableTable, InlineEditInput, DeleteConfirmationModal],
  selector: 'app-task-list',
  styleUrl: './task-list.css',
  templateUrl: './task-list.html',
})
export class TaskList {
  readonly tasks = input.required<Task[]>();

  readonly taskEditEvent = output<Task>();
  readonly taskDeleteEvent = output<Task>();

  readonly columns: SortableColumn<Task>[] = [
    {
      key: "task",
      label: "Name",
      align: "left",
      sortValue: (task) => task.name
    }
  ];

  trackBy(_index: number, task: Task): unknown {
    return task.id;
  }

  readonly editedTask = signal<Task | null>(null);
  readonly deleteConfirmationModalTask = signal<Task | null>(null);

  startEditingTaskName(task: Task): void {
    this.editedTask.set(task);
  }

  editTaskName(newName: string): void {
    const task: Task = { ...this.editedTask()!, name: newName };

    this.taskEditEvent.emit(task);
    this.stopEditingTaskName();
  }

  stopEditingTaskName(): void {
    this.editedTask.set(null);
  }

  openDeleteConfirmationModal(task: Task): void {
    this.deleteConfirmationModalTask.set(task);
  }

  handleDeleteConfirmation(confirmedDeletion: boolean): void {
    if (confirmedDeletion) {
      const toDelete: Task | null = this.deleteConfirmationModalTask();
      if (toDelete != null) {
        this.taskDeleteEvent.emit(toDelete);
      }
    }
    this.closeDeleteConfirmationModal();
  }

  closeDeleteConfirmationModal(): void {
    this.deleteConfirmationModalTask.set(null);
  }
}
