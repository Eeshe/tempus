import { HttpErrorResponse } from '@angular/common/http';
import { Component, inject, output, signal } from '@angular/core';
import { form, FormField, required, submit } from '@angular/forms/signals';
import { firstValueFrom } from 'rxjs';
import { AppModal } from '../../shared/modal/modal';
import { ModalBase } from '../../shared/modal/modal-base';
import { Client } from '../models/client.model';
import { ClientService } from '../services/client.service';

interface CreateClientModel {
  name: string;
}

@Component({
  imports: [FormField, AppModal],
  selector: 'app-create-client-form-modal',
  styleUrl: './create-client-form-modal.css',
  templateUrl: './create-client-form-modal.html',
})
export class CreateClientFormModal extends ModalBase {
  private readonly clientService: ClientService = inject(ClientService);

  readonly clientModel = signal<CreateClientModel>({
    name: "",
  });
  readonly clientForm = form(this.clientModel, (fieldPath) => {
    required(fieldPath.name, { message: "You must provide a client name" });
  });

  readonly clientCreateEvent = output<Client>();

  onSubmit(event: Event): void {
    event.preventDefault();

    submit(this.clientForm, async () => {
      try {
        const client: Client = await firstValueFrom(this.clientService.createClient(
          this.clientModel().name
        ));
        this.clientCreateEvent.emit(client);
        this.close();
        return null;
      } catch (error) {
        if (error instanceof HttpErrorResponse && error.status == 400) {
          return { kind: 'usedClientName', message: "You already have a client with this name" };
        }
        return { kind: 'serverError', message: "Something's wrong server-side" };
      }
    });
  }
}
