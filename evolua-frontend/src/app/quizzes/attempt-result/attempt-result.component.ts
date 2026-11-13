import { HttpErrorResponse } from '@angular/common/http';
import { Component, OnInit, computed, inject, input, signal } from '@angular/core';
import { Router, RouterLink } from '@angular/router';
import { TranslatePipe, TranslateService } from '@ngx-translate/core';

import { mensajeDeError } from '../../core/http/error-message';
import { formatearNumero } from '../../core/i18n/formato';
import { LanguageService } from '../../core/i18n/language.service';
import { ResultadoIntento } from '../../core/models/intento.model';
import { AttemptService } from '../../core/services/attempt.service';
import { partesEnunciado } from '../../core/util/enunciado';
import { colorActividad } from '../../core/util/nivel';
import { AlertComponent } from '../../shared/alert/alert.component';

/**Resultado de un intento finalizado con la puntuación, el desempeño por tema, recomendaciones y revisión de las respuestas.*/
@Component({
  selector: 'app-attempt-result',
  imports: [RouterLink, TranslatePipe, AlertComponent],
  templateUrl: './attempt-result.component.html',
})
export class AttemptResultComponent implements OnInit {
  private readonly intentos = inject(AttemptService);
  private readonly router = inject(Router);
  private readonly translate = inject(TranslateService);
  private readonly idioma = inject(LanguageService).actual;

  readonly attemptId = input.required<string>();
  readonly resultado = signal<ResultadoIntento | null>(null);
  readonly cargando = signal(true);
  readonly error = signal<string | null>(null);

  readonly color = colorActividad;
  readonly partes = partesEnunciado;
  readonly actividadPorTema = computed(
    () => new Map((this.resultado()?.recomendaciones ?? []).map((r) => [r.temaId, r.tipoActividad] as const)),
  );

  ngOnInit(): void {
    this.intentos.resultado(Number(this.attemptId())).subscribe({
      next: (resultado) => {
        this.resultado.set(resultado);
        this.cargando.set(false);
      },
      error: (error: unknown) => {
        if (error instanceof HttpErrorResponse && error.status === 409) {
          void this.router.navigate(['/attempts', this.attemptId()]);
          return;
        }
        this.error.set(mensajeDeError(error, this.translate));
        this.cargando.set(false);
      },
    });
  }

  numero(valor: number): string {
    return formatearNumero(valor, this.idioma());
  }

  colorTema(temaId: number): string {
    return colorActividad(this.actividadPorTema().get(temaId));
  }
}
