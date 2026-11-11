import { Routes } from '@angular/router';
import { LoginForm } from './auth/login-form/login-form';
import { RegisterForm } from './auth/register-form/register-form';
import { ClientsPage } from './client/clients-page/clients-page';
import { authGuard } from './guards/auth.guard';
import { Home } from './home/home';
import { ProjectsPage } from './project/projects-page/projects-page';
import { ReportsPage } from './reports/reports-page/reports-page';
import { AppearanceSettings } from './settings/appearance-settings/appearance-settings';
import { unsavedThemeChangesGuard } from './settings/appearance-settings/unsaved-theme-changes.guard';
import { LocalizationSettings } from './settings/localization-settings/localization-settings';
import { MigrationSettings } from './settings/migration-settings/migration-settings';
import { SettingsPage } from './settings/settings-page/settings-page';

export const routes: Routes = [
  {
    path: '',
    pathMatch: 'full',
    redirectTo: 'home',
  },
  {
    path: 'register',
    title: 'route.register',
    component: RegisterForm,
  },
  {
    path: 'login',
    title: 'route.login',
    component: LoginForm,
  },
  {
    path: 'home',
    title: 'route.home',
    component: Home,
    canActivate: [authGuard],
  },
  {
    path: 'reports',
    title: 'route.reports',
    component: ReportsPage,
    canActivate: [authGuard],
  },
  {
    path: 'projects',
    title: 'route.projects',
    component: ProjectsPage,
    canActivate: [authGuard],
  },
  {
    path: 'clients',
    title: 'route.clients',
    component: ClientsPage,
    canActivate: [authGuard],
  },
  {
    path: 'settings',
    title: 'route.settings',
    component: SettingsPage,
    canActivate: [authGuard],
    children: [
      {
        path: '',
        pathMatch: 'full',
        redirectTo: 'localization',
      },
      {
        path: 'localization',
        title: 'route.settings.localization',
        component: LocalizationSettings,
      },
      {
        path: 'appearance',
        title: 'route.settings.appearance',
        component: AppearanceSettings,
        canDeactivate: [unsavedThemeChangesGuard],
      },
      {
        path: 'migration',
        title: 'route.settings.migration',
        component: MigrationSettings,
      },
    ],
  },
];
