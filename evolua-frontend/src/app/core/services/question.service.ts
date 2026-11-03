import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';

import { Pregunta, PreguntaRequest } from '../models/pregunta.model';

/**
 * Acceso a los servicios REST de preguntas.
 */
@Injectable({ providedIn: 'root' })
export class QuestionService {
  private readonly http = inject(HttpClient);
  private readonly base = '/api/v1/questions';

  obtener(id: number): Observable<Pregunta> {
    return this.http.get<Pregunta>(`${this.base}/${id}`);
  }

  crear(solicitud: PreguntaRequest): Observable<Pregunta> {
    return this.http.post<Pregunta>(this.base, solicitud);
  }

  actualizar(id: number, solicitud: PreguntaRequest): Observable<Pregunta> {
    return this.http.put<Pregunta>(`${this.base}/${id}`, solicitud);
  }

  desactivar(id: number): Observable<void> {
    return this.http.delete<void>(`${this.base}/${id}`);
  }
}
