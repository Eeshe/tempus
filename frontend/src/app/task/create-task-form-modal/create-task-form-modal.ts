import { HttpErrorResponse } from '@angular/common/http';
import { Component, inject, input, linkedSignal, output } from '@angular/core';
import { form, FormField, required, submit } from '@angular/forms/signals';
import { firstValueFrom } from 'rxjs';
import { Project } from '../../project/models/project.model';
import { ProjectSelectorButton } from '../../project/project-selector-button/project-selector-button';
import { AppModal } from '../../shared/modal/modal';
import { ModalBase } from '../../shared/modal/modal-base';
import { Task } from '../models/task.model';
import { TaskService } from '../services/task.service';

interface CreateTaskModel {
  name: string;
  project: Project | null;
}

@Component({
  imports: [FormField, AppModal, ProjectSelectorButton],
  selector: 'app-create-task-form-modal',
  styleUrl: './create-task-form-modal.css',
  templateUrl: './create-task-form-modal.html',
})
export class CreateTaskFormModal extends ModalBase {
  private readonly taskService: TaskService = inject(TaskService);

  readonly selectedProject = input.required<Project>();
  readonly taskModel = linkedSignal<CreateTaskModel>(() => ({
    name: "",
    project: this.selectedProject(),
  }));
  readonly taskForm = form(this.taskModel, (fieldPath) => {
    required(fieldPath.name, { message: "You must provide a task name" });
    required(fieldPath.project, { message: "You must select a project" });
  });

  readonly taskCreateEvent = output<{ project: Project, task: Task }>();

  changeProject(project: Project): void {
    this.taskModel.update(model => ({
      ...model,
      project: project,
    }));
  }

  onSubmit(event: Event): void {
    event.preventDefault();

    submit(this.taskForm, async () => {
      try {
        const project: Project = this.taskModel().project!;
        const task: Task = await firstValueFrom(this.taskService.createTask(
          this.taskModel().name,
          project,
        ));
        this.taskCreateEvent.emit({ project, task });
        this.close();
        return null;
      } catch (error) {
        if (error instanceof HttpErrorResponse && (error.status == 400 || error.status == 409)) {
          return { kind: 'usedTaskName', message: "You already have a task with this name in this project" };
        }
        return { kind: 'serverError', message: "Something's wrong server-side" };
      }
    });
  }
}
