import { HttpErrorResponse, HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';
import { Router } from '@angular/router';
import { catchError, throwError } from 'rxjs';

import { AuthService } from './auth.service';

/**
 * Agrega el token JWT a las solicitudes a la API y cierra la sesión cuando el servidor responde 401.
 */
export const authInterceptor: HttpInterceptorFn = (solicitud, siguiente) => {
  const auth = inject(AuthService);
  const router = inject(Router);
  const esApi = solicitud.url.startsWith('/api/');
  const token = auth.token();
  const conToken =
    esApi && token !== null ? solicitud.clone({ setHeaders: { Authorization: `Bearer ${token}` } }) : solicitud;

  return siguiente(conToken).pipe(
    catchError((error: unknown) => {
      if (
        error instanceof HttpErrorResponse &&
        error.status === 401 &&
        esApi &&
        !solicitud.url.endsWith('/auth/login')
      ) {
        auth.cerrarSesion();
        void router.navigate(['/login'], { queryParams: { sesion: 'expirada' } });
      }
      return throwError(() => error);
    }),
  );
};
