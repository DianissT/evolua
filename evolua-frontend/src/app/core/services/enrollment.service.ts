import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';

import { Matricula } from '../models/matricula.model';

/**Acceso a los servicios REST de matrículas de un curso.*/
@Injectable({ providedIn: 'root' })
export class EnrollmentService {
  private readonly http = inject(HttpClient);

  listar(cursoId: number): Observable<Matricula[]> {
    return this.http.get<Matricula[]>(this.ruta(cursoId));
  }

  matricular(cursoId: number, correo: string): Observable<Matricula> {
    return this.http.post<Matricula>(this.ruta(cursoId), { correo });
  }

  eliminar(cursoId: number, estudianteId: number): Observable<void> {
    return this.http.delete<void>(`${this.ruta(cursoId)}/${estudianteId}`);
  }

  private ruta(cursoId: number): string {
    return `/api/v1/courses/${cursoId}/enrollments`;
  }
}
