import { HttpErrorResponse } from '@angular/common/http';
import { ChangeDetectionStrategy, Component, signal } from '@angular/core';
import { form, FormField, required, submit, validate } from '@angular/forms/signals';
import { Router, RouterLink } from '@angular/router';
import { firstValueFrom } from 'rxjs';
import { AuthService } from '../../services/auth.service';

interface RegisterData {
  username: string;
  password: string;
  passwordConfirmation: string;
}

@Component({
  selector: 'app-register-form',
  imports: [FormField, RouterLink],
  templateUrl: './register-form.html',
  changeDetection: ChangeDetectionStrategy.Eager,
  styleUrl: './register-form.css',
})
export class RegisterForm {
  registerModel = signal<RegisterData>({
    username: '',
    password: '',
    passwordConfirmation: '',
  });

  registerForm = form(this.registerModel, (fieldPath) => {
    required(fieldPath.username, { message: 'Username is required' });
    required(fieldPath.password, { message: 'Password is required' });
    required(fieldPath.passwordConfirmation, { message: 'You must confirm your password' });
    validate(fieldPath.passwordConfirmation, (value) => {
      if (value.value() === '') {
        return null;
      }
      return value.value() !== this.registerForm.password().value()
        ? {
          kind: 'passwordMismatch',
          message: 'Passwords must match',
        }
        : null;
    });
  });

  constructor(
    private authService: AuthService,
    private router: Router,
  ) { }

  doPasswordsMatch(): boolean {
    return (
      this.registerForm.password().value() === this.registerForm.passwordConfirmation().value()
    );
  }

  onSubmit(event: Event) {
    event.preventDefault();

    submit(this.registerForm, async () => {
      const username: string = this.registerModel().username;
      const password: string = this.registerModel().password;

      try {
        await firstValueFrom(this.authService.register(username, password));
        await firstValueFrom(this.authService.login(username, password));
        this.router.navigate(['/home']);

        return undefined;
      } catch (error) {
        if (error instanceof HttpErrorResponse && error.status == 400) {
          return { kind: 'usernameTaken', message: 'Username is taken. Try a different one' };
        }
        return { kind: 'serverError', message: "Something's wrong server-side" };
      }
    });
  }
}
