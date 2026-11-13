import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';

import { Quiz, QuizRequest, QuizResumen } from '../models/evaluacion.model';

/**Acceso a los servicios REST de las evaluaciones fijas.*/
@Injectable({ providedIn: 'root' })
export class QuizService {
  private readonly http = inject(HttpClient);
  private readonly base = '/api/v1/quizzes';

  listarPorCurso(cursoId: number): Observable<QuizResumen[]> {
    return this.http.get<QuizResumen[]>(`/api/v1/courses/${cursoId}/quizzes`);
  }

  obtener(id: number): Observable<Quiz> {
    return this.http.get<Quiz>(`${this.base}/${id}`);
  }

  crear(solicitud: QuizRequest): Observable<Quiz> {
    return this.http.post<Quiz>(this.base, solicitud);
  }

  actualizar(id: number, solicitud: QuizRequest): Observable<Quiz> {
    return this.http.put<Quiz>(`${this.base}/${id}`, solicitud);
  }

  desactivar(id: number): Observable<void> {
    return this.http.delete<void>(`${this.base}/${id}`);
  }
}
