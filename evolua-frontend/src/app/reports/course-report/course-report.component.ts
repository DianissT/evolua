import { DatePipe } from '@angular/common';
import { Component, OnInit, computed, inject, input, signal } from '@angular/core';
import { RouterLink } from '@angular/router';
import { TranslatePipe, TranslateService } from '@ngx-translate/core';
import { forkJoin } from 'rxjs';

import { mensajeDeError } from '../../core/http/error-message';
import { formatearNumero } from '../../core/i18n/formato';
import { LanguageService } from '../../core/i18n/language.service';
import { PreguntaDificil, RendimientoCurso } from '../../core/models/reporte.model';
import { ReportService } from '../../core/services/report.service';
import { aCsv, descargarCsv } from '../../core/util/csv';
import { AlertComponent } from '../../shared/alert/alert.component';

/**
 * Reporte de rendimiento de un curso para su docente: indicadores generales, resultado por tema, estudiantes (con
 * filtros y exportación a CSV) y las preguntas con menor porcentaje de aciertos.
 */
@Component({
  selector: 'app-course-report',
  imports: [DatePipe, RouterLink, TranslatePipe, AlertComponent],
  templateUrl: './course-report.component.html',
})
export class CourseReportComponent implements OnInit {
  private readonly api = inject(ReportService);
  private readonly translate = inject(TranslateService);
  private readonly idioma = inject(LanguageService).actual;

  readonly id = input.required<string>();
  readonly idNumerico = computed(() => Number(this.id()));

  readonly reporte = signal<RendimientoCurso | null>(null);
  readonly dificiles = signal<PreguntaDificil[]>([]);
  readonly cargando = signal(true);
  readonly error = signal<string | null>(null);

  readonly filtroTexto = signal('');
  readonly soloRefuerzo = signal(false);

  readonly estudiantes = computed(() => {
    const reporte = this.reporte();
    if (reporte === null) {
      return [];
    }
    const texto = this.filtroTexto().trim().toLowerCase();
    return reporte.estudiantes.filter(
      (e) =>
        (texto === '' || e.nombre.toLowerCase().includes(texto) || e.correo.toLowerCase().includes(texto)) &&
        (!this.soloRefuerzo() || e.temasEnRefuerzo > 0),
    );
  });

  ngOnInit(): void {
    forkJoin({
      reporte: this.api.rendimiento(this.idNumerico()),
      dificiles: this.api.preguntasDificiles(this.idNumerico()),
    }).subscribe({
      next: (datos) => {
        this.reporte.set(datos.reporte);
        this.dificiles.set(datos.dificiles);
        this.cargando.set(false);
      },
      error: (error: unknown) => {
        this.error.set(mensajeDeError(error, this.translate));
        this.cargando.set(false);
      },
    });
  }

  alCambiarTexto(evento: Event): void {
    this.filtroTexto.set((evento.target as HTMLInputElement).value);
  }

  alCambiarRefuerzo(evento: Event): void {
    this.soloRefuerzo.set((evento.target as HTMLInputElement).checked);
  }

  alCambiarTema(evento: Event): void {
    const valor = (evento.target as HTMLSelectElement).value;
    this.api.preguntasDificiles(this.idNumerico(), valor === '' ? null : Number(valor)).subscribe({
      next: (preguntas) => {
        this.error.set(null);
        this.dificiles.set(preguntas);
      },
      error: (error: unknown) => this.error.set(mensajeDeError(error, this.translate)),
    });
  }

  numero(valor: number): string {
    return formatearNumero(valor, this.idioma());
  }

  /** Descarga la tabla de estudiantes (con los filtros aplicados) como CSV. */
  exportarCsv(): void {
    const reporte = this.reporte();
    if (reporte === null) {
      return;
    }
    const t = (clave: string) => this.translate.instant(clave) as string;
    const separador = this.idioma() === 'en' ? ',' : ';'; /** Español y portuges usan coma. */
    const filas = this.estudiantes().map((e) => [
      e.nombre,
      e.correo,
      e.intentosFinalizados,
      e.promedioPuntuacion === null ? '' : this.numero(e.promedioPuntuacion),
      e.ultimaActividad ?? '',
      e.temasEnRefuerzo,
    ]);
    const contenido = aCsv(
      [t('report.student'), t('enrollments.emailColumn'), t('report.attempts'), t('report.average'), t('report.lastActivity'), t('report.weakTopics')],
      filas,
      separador,
    );
    descargarCsv(`reporte-curso-${reporte.cursoId}.csv`, contenido);
  }
}
