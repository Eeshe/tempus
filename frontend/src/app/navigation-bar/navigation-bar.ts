import { Component, inject, Signal } from '@angular/core';
import { RouterLink, RouterLinkActive } from '@angular/router';
import { AuthService } from '../services/auth.service';
import { SyncButtons } from '../sync/sync-buttons/sync-buttons';
import { ProfileButton } from './profile-button/profile-button';

@Component({
  imports: [RouterLink, RouterLinkActive, SyncButtons, ProfileButton],
  selector: 'app-navigation-bar',
  styleUrl: './navigation-bar.css',
  templateUrl: './navigation-bar.html',
})
export class NavigationBar {
  private readonly authService: AuthService = inject(AuthService);

  readonly isAuthenticated: Signal<boolean> = this.authService.isAuthenticated;
}
