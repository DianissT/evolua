import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';

import { Rol } from '../models/usuario.model';
import { AuthService } from './auth.service';

/**
 * Ruta de inicio de cada rol.
 */
export function rutaInicial(rol: Rol | null): string {
  if (rol === 'ADMIN') {
    return '/admin/audit';
  }
  return rol === null ? '/login' : '/courses';
}

/**
 * Permite el acceso solo a usuarios autenticados; si no, redirige al inicio de sesión.
 */
export const authGuard: CanActivateFn = (_ruta, estado) => {
  const auth = inject(AuthService);
  const router = inject(Router);
  return auth.autenticado() ? true : router.createUrlTree(['/login'], { queryParams: { returnUrl: estado.url } });
};

/**
 * Impide que un usuario ya autenticado vea la pantalla de inicio de sesión.
 */
export const guestGuard: CanActivateFn = () => {
  const auth = inject(AuthService);
  const router = inject(Router);
  return auth.autenticado() ? router.createUrlTree([rutaInicial(auth.rol())]) : true;
};

/**
 * Redirige la ruta raíz a la pantalla de inicio del rol del usuario.
 */
export const inicioGuard: CanActivateFn = () => {
  const auth = inject(AuthService);
  return inject(Router).createUrlTree([rutaInicial(auth.rol())]);
};

/**
 * Permite el acceso solo a los roles indicados; si no, redirige a la página de acceso denegado.
 */
export function rolGuard(...roles: Rol[]): CanActivateFn {
  return () => {
    const rol = inject(AuthService).rol();
    return rol !== null && roles.includes(rol) ? true : inject(Router).createUrlTree(['/forbidden']);
  };
}
