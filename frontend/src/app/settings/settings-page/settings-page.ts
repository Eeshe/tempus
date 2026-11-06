import { Component } from '@angular/core';
import { RouterLink, RouterLinkActive, RouterOutlet } from '@angular/router';

interface SettingsNavItem {
  readonly label: string;
  readonly route: string;
}

@Component({
  imports: [RouterLink, RouterLinkActive, RouterOutlet],
  selector: 'app-settings-page',
  styleUrl: './settings-page.css',
  templateUrl: './settings-page.html',
})
export class SettingsPage {
  readonly navItems: readonly SettingsNavItem[] = [
    { label: $localize`:Settings navigation|Language:Language`, route: 'language' },
    { label: $localize`:Settings navigation|Appearance:Appearance`, route: 'appearance' },
    { label: $localize`:Settings navigation|Migration:Migration`, route: 'migration' },
  ];
}
