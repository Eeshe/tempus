import { HttpClient } from "@angular/common/http";
import { inject, Service, Signal, signal } from "@angular/core";
import { catchError, map, Observable, of, tap } from "rxjs";

export const AUTH_BASE_URL = "/api/v1/auth";

@Service()
export class AuthService {
  private readonly http: HttpClient = inject(HttpClient);
  private readonly baseUrl = AUTH_BASE_URL;

  private readonly _isAuthenticated = signal<boolean>(false);

  readonly isAuthenticated: Signal<boolean> = this._isAuthenticated.asReadonly();

  checkAuthentication(): Observable<boolean> {
    return this.http.get(`${this.baseUrl}/me`).pipe(
      map(() => true),
      catchError(() => of(false)),
      tap(isAuthenticated => this._isAuthenticated.set(isAuthenticated))
    );
  }

  register(username: string, password: string): Observable<void> {
    return this.makePostRequest("register", username, password).pipe(
      tap(() => this.markAuthenticated())
    );
  }

  login(username: string, password: string): Observable<void> {
    return this.makePostRequest("login", username, password).pipe(
      tap(() => this.markAuthenticated())
    );
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
  }

  private makePostRequest(endpoint: string, username: string, password: string): Observable<void> {
    return this.http.post<void>(`${this.baseUrl}/${endpoint}`, { username, password });
  }
}
