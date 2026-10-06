import { Component, inject } from '@angular/core';
import { Router, RouterLink } from '@angular/router';
import { AuthService } from '../../services/auth.service';
import { PopupSelectorBase } from '../../shared/selector/popup-selector-base';

@Component({
  imports: [RouterLink],
  selector: 'app-profile-button',
  styleUrl: './profile-button.css',
  templateUrl: './profile-button.html',
})
export class ProfileButton extends PopupSelectorBase {
  private readonly authService: AuthService = inject(AuthService);
  private readonly router: Router = inject(Router);

  protected override readonly alignPopupRight: boolean = true;

  logout(): void {
    this.toggle();
    this.authService.logout().subscribe(() => this.router.navigate(['/login']));
  }
}
