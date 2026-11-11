import { HttpClient } from "@angular/common/http";
import { inject, Service, Signal, signal } from "@angular/core";
import { catchError, map, Observable, of, tap } from "rxjs";
import { Theme, User, UserSettings } from "../user/models/user.model";

export const AUTH_BASE_URL = "/api/v1/auth";

@Service()
export class AuthService {
  private readonly http: HttpClient = inject(HttpClient);
  private readonly baseUrl = AUTH_BASE_URL;

  private readonly _isAuthenticated = signal<boolean>(false);
  private readonly _currentUser = signal<User | null>(null);

  readonly isAuthenticated: Signal<boolean> = this._isAuthenticated.asReadonly();
  readonly currentUser: Signal<User | null> = this._currentUser.asReadonly();

  checkAuthentication(): Observable<boolean> {
    return this.http.get<User>(`${this.baseUrl}/me`).pipe(
      tap(user => this._currentUser.set(user)),
      map(() => true),
      catchError(() => {
        this._currentUser.set(null);
        return of(false);
      }),
      tap(isAuthenticated => this._isAuthenticated.set(isAuthenticated))
    );
  }

  register(username: string, password: string): Observable<User> {
    return this.makePostRequest("register", username, password).pipe(
      tap(user => this.storeAuthenticatedUser(user))
    );
  }

  login(username: string, password: string): Observable<User> {
    return this.makePostRequest("login", username, password).pipe(
      tap(user => this.storeAuthenticatedUser(user))
    );
  }

  private storeAuthenticatedUser(user: User): void {
    this._currentUser.set(user);
    this.markAuthenticated();
  }

  private makePostRequest(endpoint: string, username: string, password: string): Observable<User> {
    return this.http.post<User>(`${this.baseUrl}/${endpoint}`, { username, password });
  }

  logout(): Observable<void> {
    return this.http.post<void>(`${this.baseUrl}/logout`, {}, { withCredentials: true }).pipe(
      tap(() => this.markUnauthenticated())
    );
  }

  markAuthenticated(): void {
    this._isAuthenticated.set(true);
  }

  markUnauthenticated(): void {
    this._isAuthenticated.set(false);
    this._currentUser.set(null);
  }

  /**
   * Keeps the local user in sync after a settings update, so the theme is treated as saved without
   * having to re-fetch /me.
   */
  setTheme(theme: Theme): void {
    this.updateSettings({ theme });
  }

  /**
   * Keeps the local user in sync after a settings update, so the currency is treated as saved
   * without having to re-fetch /me.
   */
  setCurrency(currency: string): void {
    this.updateSettings({ currency });
  }

  private updateSettings(settings: Partial<UserSettings>): void {
    const currentUser: User | null = this._currentUser();
    if (currentUser == null) {
      return;
    }
    this._currentUser.set({
      ...currentUser,
      settings: {
        ...currentUser.settings,
        ...settings,
      },
    });
  }
}
