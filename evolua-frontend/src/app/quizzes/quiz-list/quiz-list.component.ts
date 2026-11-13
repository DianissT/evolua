import { Component, OnInit, inject, input, signal } from '@angular/core';
import { Router, RouterLink } from '@angular/router';
import { TranslatePipe, TranslateService } from '@ngx-translate/core';
import { Observable, switchMap } from 'rxjs';

import { mensajeDeError } from '../../core/http/error-message';
import { QuizResumen } from '../../core/models/evaluacion.model';
import { AttemptService } from '../../core/services/attempt.service';
import { QuizService } from '../../core/services/quiz.service';
import { AlertComponent } from '../../shared/alert/alert.component';

/**Evaluaciones de un curso. Un estudiante las inicia o retoma y un el docente las administra.*/
@Component({
  selector: 'app-quiz-list',
  imports: [RouterLink, TranslatePipe, AlertComponent],
  templateUrl: './quiz-list.component.html',
})
export class QuizListComponent implements OnInit {
  private readonly quizzesApi = inject(QuizService);
  private readonly intentosApi = inject(AttemptService);
  private readonly router = inject(Router);
  private readonly translate = inject(TranslateService);

  readonly courseId = input.required<number>();
  readonly esDocente = input(false);

  readonly quizzes = signal<QuizResumen[]>([]);
  readonly enCurso = signal<ReadonlySet<number>>(new Set<number>());
  readonly error = signal<string | null>(null);

  ngOnInit(): void {
    this.cargar();
    if (!this.esDocente()) {
      this.intentosApi.historial().subscribe({
        next: (historial) =>
          this.enCurso.set(
            new Set(
              historial
                .filter((intento) => intento.estado === 'EN_CURSO' && intento.evaluacionId !== null)
                .map((intento) => intento.evaluacionId as number),
            ),
          ),
        error: () => undefined,
      });
    }
  }

  limiteAlcanzado(quiz: QuizResumen): boolean {
    return (
      quiz.intentosPermitidos !== null &&
      (quiz.intentosUsados ?? 0) >= quiz.intentosPermitidos &&
      !this.enCurso().has(quiz.id)
    );
  }

  iniciar(quiz: QuizResumen): void {
    this.error.set(null);
    this.intentosApi.iniciar(quiz.id).subscribe({
      next: (intento) => void this.router.navigate(['/attempts', intento.id]),
      error: (error: unknown) => this.error.set(mensajeDeError(error, this.translate)),
    });
  }

  alternarActiva(quiz: QuizResumen): void {
    if (quiz.activa && !confirm(this.translate.instant('quizzes.confirmDeactivate', { name: quiz.nombre }) as string)) {
      return;
    }
    const accion: Observable<unknown> = quiz.activa
      ? this.quizzesApi.desactivar(quiz.id)
      : this.quizzesApi.obtener(quiz.id).pipe(
          switchMap((completo) =>
            this.quizzesApi.actualizar(quiz.id, {
              cursoId: completo.cursoId,
              nombre: completo.nombre,
              tipo: completo.tipo,
              intentosPermitidos: completo.intentosPermitidos,
              activa: true,
              preguntaIds: completo.preguntas.map((pregunta) => pregunta.id),
            }),
          ),
        );
    accion.subscribe({
      next: () => this.cargar(),
      error: (error: unknown) => this.error.set(mensajeDeError(error, this.translate)),
    });
  }

  private cargar(): void {
    this.quizzesApi.listarPorCurso(this.courseId()).subscribe({
      next: (quizzes) => this.quizzes.set(quizzes),
      error: (error: unknown) => this.error.set(mensajeDeError(error, this.translate)),
    });
  }
}
