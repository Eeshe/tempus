import { HttpErrorResponse } from '@angular/common/http';
import { Component, inject, output, signal } from '@angular/core';
import { form, FormField, min, required, submit } from '@angular/forms/signals';
import { firstValueFrom } from 'rxjs';
import { ClientSelectorButton } from '../../client/client-selector-button/client-selector-button';
import { Client } from '../../client/models/client.model';
import { ProjectService } from '../../services/project.service';
import { AppModal } from '../../shared/modal/modal';
import { ModalBase } from '../../shared/modal/modal-base';
import { Project } from '../models/project.model';

interface CreateProjectModel {
  name: string;
  isPrivate: boolean;
  client: Client | null;
  hourlyRate: number | null;
}

@Component({
  imports: [FormField, AppModal, ClientSelectorButton],
  selector: 'app-create-project-form-modal',
  styleUrl: './create-project-form-modal.css',
  templateUrl: './create-project-form-modal.html',
})
export class CreateProjectFormModal extends ModalBase {
  private readonly projectService: ProjectService = inject(ProjectService);

  readonly projectModel = signal<CreateProjectModel>({
    name: "",
    isPrivate: true,
    client: null,
    hourlyRate: null,
  });
  readonly projectForm = form(this.projectModel, (fieldPath) => {
    required(fieldPath.name, { message: "You must provide a project name" });
    required(fieldPath.isPrivate);
    min(fieldPath.hourlyRate, 0, { message: "Hourly rate cannot be negative" });
  });

  readonly projectCreateEvent = output<Project>();

  changeClient(client: Client | null): void {
    this.projectModel.update(model => ({
      ...model,
      client: client,
    }));
  }

  onSubmit(event: Event): void {
    event.preventDefault();

    submit(this.projectForm, async () => {
      try {
        const project: Project = await firstValueFrom(this.projectService.createProject(
          this.projectModel().name,
          this.projectModel().isPrivate,
          this.projectModel().client,
          this.projectModel().hourlyRate
        ));
        this.projectCreateEvent.emit(project);
        this.close();
        return null;
      } catch (error) {
        if (error instanceof HttpErrorResponse && error.status == 400) {
          return { kind: 'usedProjectName', message: "You already have a project with this name" };
        }
        return { kind: 'serverError', message: "Something's wrong server-side" };
      }
    });
  }
}
