import { HttpErrorResponse, HttpInterceptorFn } from "@angular/common/http";
import { inject } from "@angular/core";
import { Router } from "@angular/router";
import { catchError, throwError } from "rxjs";
import { AUTH_BASE_URL, AuthService } from "../services/auth.service";

/**
 * Clears the local authentication state and redirects to the login page when
 * the backend rejects a request because the session is no longer valid.
 * Requests to the auth endpoints themselves are excluded, so a failed login
 * attempt or the initial session check does not trigger a redirect.
 */
export const authErrorInterceptor: HttpInterceptorFn = (request, next) => {
  const authService = inject(AuthService);
  const router = inject(Router);

  return next(request).pipe(
    catchError((error: HttpErrorResponse) => {
      const isAuthRequest = request.url.startsWith(AUTH_BASE_URL);
      const isSyncRequest = request.url.startsWith("/api/v1/sync");

      if (error.status === 401 && !isAuthRequest && !isSyncRequest && router.url !== "/login") {
        authService.markUnauthenticated();
        router.navigate(["/login"]);
      }
      return throwError(() => error);
    })
  );
};
