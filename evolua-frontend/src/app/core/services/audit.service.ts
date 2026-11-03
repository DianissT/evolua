import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';

import { RegistroAuditoria } from '../models/auditoria.model';

/**
 * Acceso al servicio REST de auditoría.
 */
@Injectable({ providedIn: 'root' })
export class AuditService {
  private readonly http = inject(HttpClient);
  private readonly base = '/api/v1/admin/audit';

  listar(): Observable<RegistroAuditoria[]> {
    return this.http.get<RegistroAuditoria[]>(this.base);
  }
}
