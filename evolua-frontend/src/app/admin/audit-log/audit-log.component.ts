import { DatePipe } from '@angular/common';
import { Component, OnInit, computed, inject, signal } from '@angular/core';
import { TranslatePipe, TranslateService } from '@ngx-translate/core';

import { mensajeDeError } from '../../core/http/error-message';
import { RegistroAuditoria } from '../../core/models/auditoria.model';
import { AuditService } from '../../core/services/audit.service';
import { AlertComponent } from '../../shared/alert/alert.component';

/**
 * Consulta de los registros de auditoría con filtros por acción y por texto. Solo para administradores.
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
  readonly filtroAccion = signal('');
  readonly filtroTexto = signal('');

  readonly acciones = computed(() => [...new Set(this.registros().map((r) => r.accion))].sort());

  readonly filtrados = computed(() => {
    const accion = this.filtroAccion();
    const texto = this.filtroTexto().trim().toLowerCase();
    return this.registros().filter((r) => {
      const contenido = `${r.usuarioNombre ?? ''} ${r.entidad} ${r.detalle ?? ''}`.toLowerCase();
      return (accion === '' || r.accion === accion) && (texto === '' || contenido.includes(texto));
    });
  });

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

  alCambiarAccion(evento: Event): void {
    this.filtroAccion.set((evento.target as HTMLSelectElement).value);
  }

  alCambiarTexto(evento: Event): void {
    this.filtroTexto.set((evento.target as HTMLInputElement).value);
  }
}
