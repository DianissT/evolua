import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';

import { ProgresoTema, Recomendacion } from '../models/progreso.model';

/**Acceso a los servicios REST de progreso y recomendaciones de los estudiantes.*/
@Injectable({ providedIn: 'root' })
export class ProgressService {
  private readonly http = inject(HttpClient);

  miProgreso(): Observable<ProgresoTema[]> {
    return this.http.get<ProgresoTema[]>('/api/v1/progress/me');
  }

  misRecomendaciones(): Observable<Recomendacion[]> {
    return this.http.get<Recomendacion[]>('/api/v1/recommendations/me');
  }
}
