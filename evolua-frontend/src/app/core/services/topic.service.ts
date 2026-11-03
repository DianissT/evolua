import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';

import { Tema, TemaRequest } from '../models/curso.model';
import { Pregunta } from '../models/pregunta.model';

/**
 * Acceso a los servicios REST de temas.
 */
@Injectable({ providedIn: 'root' })
export class TopicService {
  private readonly http = inject(HttpClient);
  private readonly base = '/api/v1/topics';

  crear(solicitud: TemaRequest): Observable<Tema> {
    return this.http.post<Tema>(this.base, solicitud);
  }

  actualizar(id: number, solicitud: TemaRequest): Observable<Tema> {
    return this.http.put<Tema>(`${this.base}/${id}`, solicitud);
  }

  desactivar(id: number): Observable<void> {
    return this.http.delete<void>(`${this.base}/${id}`);
  }

  preguntas(id: number): Observable<Pregunta[]> {
    return this.http.get<Pregunta[]>(`${this.base}/${id}/questions`);
  }
}
