import { HttpErrorResponse } from '@angular/common/http';
import { ChangeDetectionStrategy, Component, signal } from '@angular/core';
import { form, FormField, required, submit } from '@angular/forms/signals';
import { Router, RouterLink } from '@angular/router';
import { firstValueFrom } from 'rxjs';
import { AuthService } from '../../services/auth.service';

interface LoginData {
  username: string;
  password: string;
}

@Component({
  selector: 'app-login-form',
  imports: [FormField, RouterLink],
  templateUrl: './login-form.html',
  changeDetection: ChangeDetectionStrategy.Eager,
  styleUrl: './login-form.css',
})
export class LoginForm {
  loginModel = signal<LoginData>({
    username: '',
    password: '',
  });

  loginForm = form(this.loginModel, (fieldPath) => {
    required(fieldPath.username, { message: 'Username is required' });
    required(fieldPath.password, { message: 'Pasword is required' });
  });

  constructor(
    private authService: AuthService,
    private router: Router,
  ) { }

  onSubmit(event: Event) {
    event.preventDefault();

    submit(this.loginForm, async () => {
      try {
        await firstValueFrom(
          this.authService.login(this.loginModel().username, this.loginModel().password),
        );
        this.router.navigate(['/home']);
        return undefined;
      } catch (error) {
        if (error instanceof HttpErrorResponse && error.status == 401) {
          return { kind: 'invalidCredentials', message: 'Invalid credentials' };
        }
        return { kind: 'serverError', message: "Something's wrong server-side" };
      }
    });
  }
}
