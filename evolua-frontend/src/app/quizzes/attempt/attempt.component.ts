import { HttpErrorResponse } from '@angular/common/http';
import { Component, OnInit, computed, inject, input, signal } from '@angular/core';
import { Router } from '@angular/router';
import { TranslatePipe, TranslateService } from '@ngx-translate/core';

import { mensajeDeError } from '../../core/http/error-message';
import { Intento } from '../../core/models/intento.model';
import { AttemptService } from '../../core/services/attempt.service';
import { partesEnunciado } from '../../core/util/enunciado';
import { AlertComponent } from '../../shared/alert/alert.component';

/**Pantalla para responder una evaluación con una pregunta a la vez y guardando cada respuesta al elegirla.*/
@Component({
  selector: 'app-attempt',
  imports: [TranslatePipe, AlertComponent],
  templateUrl: './attempt.component.html',
  styleUrl: './attempt.component.scss',
})
export class AttemptComponent implements OnInit {
  private readonly intentos = inject(AttemptService);
  private readonly router = inject(Router);
  private readonly translate = inject(TranslateService);

  readonly attemptId = input.required<string>();
  readonly idNumerico = computed(() => Number(this.attemptId()));

  readonly intento = signal<Intento | null>(null);
  readonly respuestas = signal<Record<number, number>>({});
  readonly indice = signal(0);
  readonly pendientes = signal(0);
  readonly finalizando = signal(false);
  readonly cargando = signal(true);
  readonly error = signal<string | null>(null);

  readonly pregunta = computed(() => this.intento()?.preguntas[this.indice()] ?? null);
  readonly partes = computed(() => {
    const pregunta = this.pregunta();
    return pregunta ? partesEnunciado(pregunta.enunciado) : null;
  });
  readonly total = computed(() => this.intento()?.preguntas.length ?? 0);
  readonly progreso = computed(() =>
    this.total() === 0 ? 0 : Math.round(((this.indice() + 1) * 100) / this.total()),
  );
  readonly sinResponder = computed(() => this.total() - Object.keys(this.respuestas()).length);
  readonly esUltima = computed(() => this.indice() === this.total() - 1);

  ngOnInit(): void {
    this.intentos.obtener(this.idNumerico()).subscribe({
      next: (intento) => {
        const guardadas: Record<number, number> = {};
        intento.preguntas.forEach((pregunta) => {
          if (pregunta.opcionSeleccionadaId !== null) {
            guardadas[pregunta.id] = pregunta.opcionSeleccionadaId;
          }
        });
        this.intento.set(intento);
        this.respuestas.set(guardadas);
        this.indice.set(Math.max(0, intento.preguntas.findIndex((pregunta) => pregunta.opcionSeleccionadaId === null)));
        this.cargando.set(false);
      },
      error: (error: unknown) => {
        if (error instanceof HttpErrorResponse && error.status === 409) {
          void this.router.navigate(['/attempts', this.idNumerico(), 'result']);
          return;
        }
        this.error.set(mensajeDeError(error, this.translate));
        this.cargando.set(false);
      },
    });
  }

  seleccionar(preguntaId: number, opcionId: number): void {
    const anterior = this.respuestas()[preguntaId];
    this.respuestas.update((actuales) => ({ ...actuales, [preguntaId]: opcionId }));
    this.error.set(null);
    this.pendientes.update((cantidad) => cantidad + 1);
    this.intentos.responder(this.idNumerico(), { preguntaId, opcionId }).subscribe({
      next: () => this.pendientes.update((cantidad) => cantidad - 1),
      error: (error: unknown) => {
        this.pendientes.update((cantidad) => cantidad - 1);
        this.respuestas.update((actuales) => {
          const copia = { ...actuales };
          if (anterior === undefined) {
            delete copia[preguntaId];
          } else {
            copia[preguntaId] = anterior;
          }
          return copia;
        });
        this.error.set(mensajeDeError(error, this.translate));
      },
    });
  }

  ir(indice: number): void {
    this.indice.set(Math.min(Math.max(indice, 0), this.total() - 1));
  }

  anterior(): void {
    this.ir(this.indice() - 1);
  }

  siguiente(): void {
    this.ir(this.indice() + 1);
  }

  finalizar(): void {
    const faltan = this.sinResponder();
    if (faltan > 0 && !confirm(this.translate.instant('attempt.confirmUnanswered', { count: faltan }) as string)) {
      return;
    }
    this.finalizando.set(true);
    this.intentos.finalizar(this.idNumerico()).subscribe({
      next: () => void this.router.navigate(['/attempts', this.idNumerico(), 'result']),
      error: (error: unknown) => {
        this.finalizando.set(false);
        this.error.set(mensajeDeError(error, this.translate));
      },
    });
  }
}
