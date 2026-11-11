export interface Theme {
  background: string;
  surface: string;
  text: string;
  accent: string;
  danger: string;
}

export type ThemeField = keyof Theme;

export interface UserSettings {
  theme: Theme;
  currency: string;
}

export interface User {
  id: number;
  name: string;
  settings: UserSettings;
  createdAt: string;
}
