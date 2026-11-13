import { DatePipe } from '@angular/common';
import { Component, OnInit, computed, inject, signal } from '@angular/core';
import { RouterLink } from '@angular/router';
import { TranslatePipe, TranslateService } from '@ngx-translate/core';
import { forkJoin } from 'rxjs';

import { mensajeDeError } from '../../core/http/error-message';
import { formatearNumero } from '../../core/i18n/formato';
import { LanguageService } from '../../core/i18n/language.service';
import { IntentoResumen } from '../../core/models/intento.model';
import { ProgresoTema, Recomendacion } from '../../core/models/progreso.model';
import { AttemptService } from '../../core/services/attempt.service';
import { ProgressService } from '../../core/services/progress.service';
import { claseNivel, colorActividad, colorNivel } from '../../core/util/nivel';
import { AlertComponent } from '../../shared/alert/alert.component';

/**Progreso del estudiante con el nivel por tema, recomendaciones vigentes, evolución de sus puntuaciones e historial sus de intentos.*/
@Component({
  selector: 'app-my-progress',
  imports: [DatePipe, RouterLink, TranslatePipe, AlertComponent],
  templateUrl: './my-progress.component.html',
})
export class MyProgressComponent implements OnInit {
  private readonly progresoApi = inject(ProgressService);
  private readonly intentosApi = inject(AttemptService);
  private readonly translate = inject(TranslateService);
  private readonly idioma = inject(LanguageService).actual;

  readonly temas = signal<ProgresoTema[]>([]);
  readonly recomendaciones = signal<Recomendacion[]>([]);
  readonly historial = signal<IntentoResumen[]>([]);
  readonly cargando = signal(true);
  readonly error = signal<string | null>(null);

  readonly claseNivel = claseNivel;
  readonly colorNivel = colorNivel;
  readonly colorActividad = colorActividad;

  readonly evolucion = computed(() =>
    this.historial()
      .filter((intento) => intento.estado === 'FINALIZADO' && intento.puntuacion !== null)
      .sort((a, b) => a.fechaInicio.localeCompare(b.fechaInicio))
      .slice(-8),
  );

  ngOnInit(): void {
    forkJoin({
      temas: this.progresoApi.miProgreso(),
      recomendaciones: this.progresoApi.misRecomendaciones(),
      historial: this.intentosApi.historial(),
    }).subscribe({
      next: (datos) => {
        this.temas.set(datos.temas);
        this.recomendaciones.set(datos.recomendaciones);
        this.historial.set(datos.historial);
        this.cargando.set(false);
      },
      error: (error: unknown) => {
        this.error.set(mensajeDeError(error, this.translate));
        this.cargando.set(false);
      },
    });
  }

  numero(valor: number): string {
    return formatearNumero(valor, this.idioma());
  }
}
