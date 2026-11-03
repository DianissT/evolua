import { Component, OnInit, computed, inject, input, signal } from '@angular/core';
import { RouterLink } from '@angular/router';
import { TranslatePipe, TranslateService } from '@ngx-translate/core';
import { Observable } from 'rxjs';

import { mensajeDeError } from '../../core/http/error-message';
import { Pregunta } from '../../core/models/pregunta.model';
import { CourseService } from '../../core/services/course.service';
import { QuestionService } from '../../core/services/question.service';
import { TopicService } from '../../core/services/topic.service';
import { AlertComponent } from '../../shared/alert/alert.component';
import { construirSolicitud } from '../question-form/question-form.util';

/**
 * Lista de las preguntas de un tema, con acciones para editarlas, desactivarlas y reactivarlas.
 */
@Component({
  selector: 'app-question-list',
  imports: [RouterLink, TranslatePipe, AlertComponent],
  templateUrl: './question-list.component.html',
})
export class QuestionListComponent implements OnInit {
  private readonly cursos = inject(CourseService);
  private readonly temas = inject(TopicService);
  private readonly preguntasApi = inject(QuestionService);
  private readonly translate = inject(TranslateService);

  readonly courseId = input.required<string>();
  readonly topicId = input.required<string>();
  readonly cursoNumerico = computed(() => Number(this.courseId()));
  readonly temaNumerico = computed(() => Number(this.topicId()));

  readonly nombreTema = signal('');
  readonly preguntas = signal<Pregunta[]>([]);
  readonly cargando = signal(true);
  readonly error = signal<string | null>(null);

  ngOnInit(): void {
    this.cursos.temas(this.cursoNumerico()).subscribe({
      next: (temas) => this.nombreTema.set(temas.find((tema) => tema.id === this.temaNumerico())?.nombre ?? ''),
      error: (error: unknown) => this.error.set(mensajeDeError(error, this.translate)),
    });
    this.cargar();
  }

  alternarActiva(pregunta: Pregunta): void {
    if (pregunta.activa) {
      if (!confirm(this.translate.instant('questions.confirmDeactivate') as string)) {
        return;
      }
    }
    const accion: Observable<unknown> = pregunta.activa
      ? this.preguntasApi.desactivar(pregunta.id)
      : this.preguntasApi.actualizar(
          pregunta.id,
          construirSolicitud(
            {
              temaId: pregunta.temaId,
              dificultad: pregunta.dificultad,
              tipo: pregunta.tipo,
              enunciado: pregunta.enunciado,
              retroalimentacion: pregunta.retroalimentacion ?? '',
              correcta: pregunta.opciones.findIndex((opcion) => opcion.esCorrecta),
              opciones: pregunta.opciones.map((opcion) => opcion.texto),
            },
            true,
          ),
        );
    accion.subscribe({
      next: () => this.cargar(),
      error: (error: unknown) => this.error.set(mensajeDeError(error, this.translate)),
    });
  }

  private cargar(): void {
    this.cargando.set(true);
    this.temas.preguntas(this.temaNumerico()).subscribe({
      next: (preguntas) => {
        this.preguntas.set(preguntas);
        this.error.set(null);
        this.cargando.set(false);
      },
      error: (error: unknown) => {
        this.error.set(mensajeDeError(error, this.translate));
        this.cargando.set(false);
      },
    });
  }
}
