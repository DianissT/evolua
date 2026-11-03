import { HttpErrorResponse } from '@angular/common/http';
import { TranslateService } from '@ngx-translate/core';

interface ErrorApi {
  mensaje?: string;
  detalles?: string[];
}

/**
 * Convierte un error HTTP en un mensaje para el usuario. Los errores 400 y 409 muestran el mensaje del servidor;
 * los demás usan textos traducidos.
 */
export function mensajeDeError(error: unknown, translate: TranslateService): string {
  if (!(error instanceof HttpErrorResponse) || error.status === 0) {
    return translate.instant('errors.network') as string;
  }
  const cuerpo = (error.error ?? {}) as ErrorApi;
  if ((error.status === 400 || error.status === 409) && cuerpo.mensaje) {
    const detalles = cuerpo.detalles && cuerpo.detalles.length > 0 ? ` (${cuerpo.detalles.join('; ')})` : '';
    return `${cuerpo.mensaje}${detalles}`;
  }
  const clave = [401, 403, 404].includes(error.status) ? `errors.${error.status}` : 'errors.generic';
  return translate.instant(clave) as string;
}
