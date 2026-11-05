import { Routes } from '@angular/router';
import { LoginForm } from './auth/login-form/login-form';
import { RegisterForm } from './auth/register-form/register-form';
import { ClientsPage } from './client/clients-page/clients-page';
import { authGuard } from './guards/auth.guard';
import { Home } from './home/home';
import { ProjectsPage } from './project/projects-page/projects-page';
import { ReportsPage } from './reports/reports-page/reports-page';
import { LanguageSettings } from './settings/language-settings/language-settings';
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
        redirectTo: 'language',
      },
      {
        path: 'language',
        title: 'route.settings.language',
        component: LanguageSettings,
      },
      {
        path: 'migration',
        title: 'route.settings.migration',
        component: MigrationSettings,
      },
    ],
  },
];
