import { HttpErrorResponse, HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';
import { catchError, throwError } from 'rxjs';
import { AuthService } from './auth.service';

/**
 * Agrega "Authorization: Bearer <token>" a cada llamada a la API.
 * Si el backend responde 401 (token vencido o inválido), cierra la sesión.
 */
export const authInterceptor: HttpInterceptorFn = (req, next) => {
  const auth = inject(AuthService);
  const token = auth.token();
  const peticion = token && req.url.startsWith('/api/')
    ? req.clone({ setHeaders: { Authorization: `Bearer ${token}` } })
    : req;

  return next(peticion).pipe(
    catchError((error: HttpErrorResponse) => {
      if (error.status === 401 && !req.url.endsWith('/auth/login')) {
        auth.logout();
      }
      return throwError(() => error);
    }),
  );
};
