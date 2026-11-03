import { DatePipe } from '@angular/common';
import { Component, OnInit, inject, signal } from '@angular/core';
import { TranslatePipe, TranslateService } from '@ngx-translate/core';

import { mensajeDeError } from '../../core/http/error-message';
import { RegistroAuditoria } from '../../core/models/auditoria.model';
import { AuditService } from '../../core/services/audit.service';
import { AlertComponent } from '../../shared/alert/alert.component';

/**
 * Consulta de los registros de auditoría. Disponible solo para administradores.
 */
@Component({
  selector: 'app-audit-log',
  imports: [DatePipe, TranslatePipe, AlertComponent],
  templateUrl: './audit-log.component.html',
})
export class AuditLogComponent implements OnInit {
  private readonly servicio = inject(AuditService);
  private readonly translate = inject(TranslateService);

  readonly registros = signal<RegistroAuditoria[]>([]);
  readonly cargando = signal(true);
  readonly error = signal<string | null>(null);

  ngOnInit(): void {
    this.servicio.listar().subscribe({
      next: (registros) => {
        this.registros.set(registros);
        this.cargando.set(false);
      },
      error: (error: unknown) => {
        this.error.set(mensajeDeError(error, this.translate));
        this.cargando.set(false);
      },
    });
  }
}
