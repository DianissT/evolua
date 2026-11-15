import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';

import { PreguntaDificil, RendimientoCurso } from '../models/reporte.model';

/**
 * Acceso a los servicios REST de reportes del docente.
 */
@Injectable({ providedIn: 'root' })
export class ReportService {
  private readonly http = inject(HttpClient);
  private readonly base = '/api/v1/reports/courses';

  rendimiento(cursoId: number): Observable<RendimientoCurso> {
    return this.http.get<RendimientoCurso>(`${this.base}/${cursoId}/performance`);
  }

  /** Las preguntas con menor porcentaje de aciertos; se puede filtrar por tema. */
  preguntasDificiles(cursoId: number, temaId: number | null = null): Observable<PreguntaDificil[]> {
    let params = new HttpParams();
    if (temaId !== null) {
      params = params.set('temaId', temaId);
    }
    return this.http.get<PreguntaDificil[]>(`${this.base}/${cursoId}/hard-questions`, { params });
  }
}
