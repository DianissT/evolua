import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';

import { Intento, IntentoResumen, RespuestaSolicitud, ResultadoIntento } from '../models/intento.model';

/**
 * Acceso a los servicios REST de intentos de las evaluaciones.
 */
@Injectable({ providedIn: 'root' })
export class AttemptService {
  private readonly http = inject(HttpClient);

  iniciar(evaluacionId: number): Observable<Intento> {
    return this.http.post<Intento>(`/api/v1/quizzes/${evaluacionId}/attempts`, null);
  }

  obtener(id: number): Observable<Intento> {
    return this.http.get<Intento>(`/api/v1/attempts/${id}`);
  }

  responder(id: number, solicitud: RespuestaSolicitud): Observable<RespuestaSolicitud> {
    return this.http.post<RespuestaSolicitud>(`/api/v1/attempts/${id}/answers`, solicitud);
  }

  finalizar(id: number): Observable<ResultadoIntento> {
    return this.http.post<ResultadoIntento>(`/api/v1/attempts/${id}/submit`, null);
  }

  resultado(id: number): Observable<ResultadoIntento> {
    return this.http.get<ResultadoIntento>(`/api/v1/attempts/${id}/result`);
  }

  historial(): Observable<IntentoResumen[]> {
    return this.http.get<IntentoResumen[]>('/api/v1/attempts');
  }
}
