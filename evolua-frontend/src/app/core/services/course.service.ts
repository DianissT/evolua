import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';

import { Curso, CursoRequest, Tema } from '../models/curso.model';

/**
 * Acceso a los servicios REST de cursos.
 */
@Injectable({ providedIn: 'root' })
export class CourseService {
  private readonly http = inject(HttpClient);
  private readonly base = '/api/v1/courses';

  listar(): Observable<Curso[]> {
    return this.http.get<Curso[]>(this.base);
  }

  obtener(id: number): Observable<Curso> {
    return this.http.get<Curso>(`${this.base}/${id}`);
  }

  crear(solicitud: CursoRequest): Observable<Curso> {
    return this.http.post<Curso>(this.base, solicitud);
  }

  actualizar(id: number, solicitud: CursoRequest): Observable<Curso> {
    return this.http.put<Curso>(`${this.base}/${id}`, solicitud);
  }

  desactivar(id: number): Observable<void> {
    return this.http.delete<void>(`${this.base}/${id}`);
  }

  temas(id: number): Observable<Tema[]> {
    return this.http.get<Tema[]>(`${this.base}/${id}/topics`);
  }
}
