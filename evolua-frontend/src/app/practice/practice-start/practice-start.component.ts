import { Component, OnInit, computed, inject, input, signal } from '@angular/core';
import { toSignal } from '@angular/core/rxjs-interop';
import { FormControl, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router } from '@angular/router';
import { TranslatePipe, TranslateService } from '@ngx-translate/core';
import { forkJoin } from 'rxjs';

import { mensajeDeError } from '../../core/http/error-message';
import { ModalidadPractica, PracticaSolicitud } from '../../core/models/intento.model';
import { ProgresoTema, Recomendacion } from '../../core/models/progreso.model';
import { AttemptService } from '../../core/services/attempt.service';
import { ProgressService } from '../../core/services/progress.service';
import { claseNivel } from '../../core/util/nivel';
import { AlertComponent } from '../../shared/alert/alert.component';

interface GrupoCurso {
  cursoNombre: string;
  temas: ProgresoTema[];
}

/**
 * Pantalla para prácticas por tema. Al crearse la práctica se responde en la misma pantalla de intentos que las evaluaciones.
 */
@Component({
  selector: 'app-practice-start',
  imports: [ReactiveFormsModule, TranslatePipe, AlertComponent],
  templateUrl: './practice-start.component.html',
})
export class PracticeStartComponent implements OnInit {
  private readonly progresoApi = inject(ProgressService);
  private readonly intentosApi = inject(AttemptService);
  private readonly router = inject(Router);
  private readonly translate = inject(TranslateService);

  readonly temaId = input<string>();
  readonly temas = signal<ProgresoTema[]>([]);
  readonly recomendaciones = signal<Recomendacion[]>([]);
  readonly cargando = signal(true);
  readonly enviando = signal(false);
  readonly enviado = signal(false);
  readonly error = signal<string | null>(null);

  readonly claseNivel = claseNivel;

  readonly form = new FormGroup({
    tema: new FormControl<number | null>(null, Validators.required),
    modalidad: new FormControl<ModalidadPractica>('AUTOMATICA', { nonNullable: true }),
    dificultad: new FormControl<number | null>(null),
    cantidad: new FormControl<number | null>(null, [Validators.min(1), Validators.max(20)]),
  });

  private readonly temaElegido = toSignal(this.form.controls.tema.valueChanges, {
    initialValue: this.form.controls.tema.value,
  });
  private readonly modalidad = toSignal(this.form.controls.modalidad.valueChanges, {
    initialValue: this.form.controls.modalidad.value,
  });

  readonly esLibre = computed(() => this.modalidad() === 'LIBRE');
  readonly progresoTema = computed(() => this.temas().find((tema) => tema.temaId === this.temaElegido()) ?? null);
  readonly recomendacionTema = computed(
    () => this.recomendaciones().find((rec) => rec.temaId === this.temaElegido()) ?? null,
  );
  /** Temas agrupados por curso para mostrarlos en el selector. */
  readonly grupos = computed<GrupoCurso[]>(() => {
    const porCurso = new Map<number, GrupoCurso>();
    for (const tema of this.temas()) {
      const grupo = porCurso.get(tema.cursoId) ?? { cursoNombre: tema.cursoNombre, temas: [] };
      grupo.temas.push(tema);
      porCurso.set(tema.cursoId, grupo);
    }
    return [...porCurso.values()];
  });

  ngOnInit(): void {
    forkJoin({ temas: this.progresoApi.miProgreso(), recomendaciones: this.progresoApi.misRecomendaciones() }).subscribe({
      next: (datos) => {
        this.temas.set(datos.temas);
        this.recomendaciones.set(datos.recomendaciones);
        const preseleccionado = Number(this.temaId());
        if (datos.temas.some((tema) => tema.temaId === preseleccionado)) {
          this.form.controls.tema.setValue(preseleccionado);
        }
        this.cargando.set(false);
      },
      error: (error: unknown) => {
        this.error.set(mensajeDeError(error, this.translate));
        this.cargando.set(false);
      },
    });
  }

  iniciar(): void {
    this.enviado.set(true);
    const valores = this.form.getRawValue();
    if (this.form.invalid || valores.tema === null || this.enviando()) {
      return;
    }
    const solicitud: PracticaSolicitud =
      valores.modalidad === 'LIBRE'
        ? { modalidad: 'LIBRE', dificultad: valores.dificultad, cantidad: valores.cantidad }
        : { modalidad: 'AUTOMATICA' };
    this.enviando.set(true);
    this.error.set(null);
    this.intentosApi.iniciarPractica(valores.tema, solicitud).subscribe({
      next: (intento) => void this.router.navigate(['/attempts', intento.id]),
      error: (error: unknown) => {
        this.enviando.set(false);
        this.error.set(mensajeDeError(error, this.translate));
      },
    });
  }
}
