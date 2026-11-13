import { Component, OnInit, computed, inject, input, signal } from '@angular/core';
import { NonNullableFormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { TranslatePipe, TranslateService } from '@ngx-translate/core';
import { Observable, forkJoin, map, of, switchMap } from 'rxjs';

import { mensajeDeError } from '../../core/http/error-message';
import { Tema } from '../../core/models/curso.model';
import { QuizRequest, TipoEvaluacion } from '../../core/models/evaluacion.model';
import { Pregunta } from '../../core/models/pregunta.model';
import { CourseService } from '../../core/services/course.service';
import { QuizService } from '../../core/services/quiz.service';
import { TopicService } from '../../core/services/topic.service';
import { partesEnunciado } from '../../core/util/enunciado';
import { AlertComponent } from '../../shared/alert/alert.component';

interface GrupoPreguntas {
  tema: Tema;
  preguntas: Pregunta[];
}

/**Formulario del docente para crear o editar una evaluación fija eligiendo preguntas de los temas del curso.
 * Si recibe el identificador de la evaluación, edita la existente.
 */
@Component({
  selector: 'app-quiz-form',
  imports: [ReactiveFormsModule, RouterLink, TranslatePipe, AlertComponent],
  templateUrl: './quiz-form.component.html',
})
export class QuizFormComponent implements OnInit {
  private readonly fb = inject(NonNullableFormBuilder);
  private readonly cursos = inject(CourseService);
  private readonly temasApi = inject(TopicService);
  private readonly quizzes = inject(QuizService);
  private readonly router = inject(Router);
  private readonly translate = inject(TranslateService);

  readonly courseId = input.required<string>();
  readonly quizId = input<string>();
  readonly cursoNumerico = computed(() => Number(this.courseId()));
  readonly quizNumerico = computed(() => (this.quizId() ? Number(this.quizId()) : null));

  readonly tipos: TipoEvaluacion[] = ['INICIAL', 'FORMATIVA', 'CONFIRMACION'];
  readonly partes = partesEnunciado;

  readonly formulario = this.fb.group({
    nombre: ['', [Validators.required, Validators.maxLength(150)]],
    tipo: this.fb.control<TipoEvaluacion>('FORMATIVA'),
    intentosPermitidos: this.fb.control<number | null>(null, [Validators.min(1)]),
  });

  readonly grupos = signal<GrupoPreguntas[]>([]);
  readonly seleccion = signal<number[]>([]);
  readonly enviado = signal(false);
  readonly guardando = signal(false);
  readonly error = signal<string | null>(null);
  private activaOriginal: boolean | undefined;

  ngOnInit(): void {
    const grupos$: Observable<GrupoPreguntas[]> = this.cursos.temas(this.cursoNumerico()).pipe(
      switchMap((temas) =>
        temas.length === 0
          ? of<GrupoPreguntas[]>([])
          : forkJoin(
              temas.map((tema) => this.temasApi.preguntas(tema.id).pipe(map((preguntas) => ({ tema, preguntas })))),
            ),
      ),
    );
    const id = this.quizNumerico();
    forkJoin({ grupos: grupos$, quiz: id === null ? of(null) : this.quizzes.obtener(id) }).subscribe({
      next: ({ grupos, quiz }) => {
        this.grupos.set(grupos);
        if (quiz !== null) {
          this.activaOriginal = quiz.activa;
          this.formulario.patchValue({
            nombre: quiz.nombre,
            tipo: quiz.tipo,
            intentosPermitidos: quiz.intentosPermitidos,
          });
          this.seleccion.set(quiz.preguntas.map((pregunta) => pregunta.id));
        }
      },
      error: (error: unknown) => this.error.set(mensajeDeError(error, this.translate)),
    });
  }

  alternar(preguntaId: number, marcada: boolean): void {
    this.seleccion.update((actual) =>
      marcada ? (actual.includes(preguntaId) ? actual : [...actual, preguntaId]) : actual.filter((id) => id !== preguntaId),
    );
  }

  guardar(): void {
    this.enviado.set(true);
    if (this.formulario.invalid || this.seleccion().length === 0) {
      return;
    }
    const orden = this.grupos()
      .flatMap((grupo) => grupo.preguntas)
      .map((pregunta) => pregunta.id)
      .filter((id) => this.seleccion().includes(id));
    const valor = this.formulario.getRawValue();
    const solicitud: QuizRequest = {
      cursoId: this.cursoNumerico(),
      nombre: valor.nombre.trim(),
      tipo: valor.tipo,
      intentosPermitidos: valor.intentosPermitidos,
      ...(this.activaOriginal === undefined ? {} : { activa: this.activaOriginal }),
      preguntaIds: orden,
    };
    const id = this.quizNumerico();
    this.guardando.set(true);
    this.error.set(null);
    const accion = id === null ? this.quizzes.crear(solicitud) : this.quizzes.actualizar(id, solicitud);
    accion.subscribe({
      next: () => void this.router.navigate(['/courses', this.cursoNumerico()]),
      error: (error: unknown) => {
        this.guardando.set(false);
        this.error.set(mensajeDeError(error, this.translate));
      },
    });
  }
}
