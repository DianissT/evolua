import { ChangeDetectorRef, Component, OnInit, computed, inject, input, signal } from '@angular/core';
import { FormArray, FormControl, NonNullableFormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { TranslatePipe, TranslateService } from '@ngx-translate/core';

import { mensajeDeError } from '../../core/http/error-message';
import { Tema } from '../../core/models/curso.model';
import { Pregunta, TipoPregunta } from '../../core/models/pregunta.model';
import { CourseService } from '../../core/services/course.service';
import { QuestionService } from '../../core/services/question.service';
import { AlertComponent } from '../../shared/alert/alert.component';
import {
  MAXIMO_OPCIONES,
  MINIMO_OPCIONES,
  ValorFormularioPregunta,
  construirSolicitud,
  opcionesIniciales,
} from './question-form.util';

/**
 * Formulario para crear o editar una pregunta con sus opciones. Si recibe el identificador de la pregunta,
 * edita la existente.
 */
@Component({
  selector: 'app-question-form',
  imports: [ReactiveFormsModule, RouterLink, TranslatePipe, AlertComponent],
  templateUrl: './question-form.component.html',
})
export class QuestionFormComponent implements OnInit {
  private readonly fb = inject(NonNullableFormBuilder);
  private readonly cursos = inject(CourseService);
  private readonly preguntas = inject(QuestionService);
  private readonly router = inject(Router);
  private readonly translate = inject(TranslateService);
  private readonly detector = inject(ChangeDetectorRef);

  readonly courseId = input.required<string>();
  readonly topicId = input.required<string>();
  readonly questionId = input<string>();

  readonly cursoNumerico = computed(() => Number(this.courseId()));
  readonly temaNumerico = computed(() => Number(this.topicId()));
  readonly preguntaNumerica = computed(() => (this.questionId() ? Number(this.questionId()) : null));

  readonly minimoOpciones = MINIMO_OPCIONES;
  readonly maximoOpciones = MAXIMO_OPCIONES;
  readonly dificultades = [1, 2, 3];
  readonly tipos: TipoPregunta[] = ['SELECCION_MULTIPLE', 'VERDADERO_FALSO'];

  readonly formulario = this.fb.group({
    temaId: [0, [Validators.required]],
    dificultad: [1, [Validators.required, Validators.min(1), Validators.max(3)]],
    tipo: this.fb.control<TipoPregunta>('SELECCION_MULTIPLE'),
    enunciado: ['', [Validators.required]],
    retroalimentacion: [''],
    correcta: [0],
    opciones: this.fb.array<FormControl<string>>([]),
  });

  readonly temas = signal<Tema[]>([]);
  readonly enviado = signal(false);
  readonly guardando = signal(false);
  readonly error = signal<string | null>(null);
  private activaOriginal: boolean | undefined;

  get opciones(): FormArray<FormControl<string>> {
    return this.formulario.controls.opciones;
  }

  esVerdaderoFalso(): boolean {
    return this.formulario.controls.tipo.value === 'VERDADERO_FALSO';
  }

  ngOnInit(): void {
    this.formulario.controls.temaId.setValue(this.temaNumerico());
    this.reiniciarOpciones('SELECCION_MULTIPLE');
    this.cursos.temas(this.cursoNumerico()).subscribe({
      next: (temas) => this.temas.set(temas.filter((tema) => tema.activo || tema.id === this.temaNumerico())),
      error: (error: unknown) => this.error.set(mensajeDeError(error, this.translate)),
    });
    const id = this.preguntaNumerica();
    if (id !== null) {
      this.preguntas.obtener(id).subscribe({
        next: (pregunta) => this.cargarPregunta(pregunta),
        error: (error: unknown) => this.error.set(mensajeDeError(error, this.translate)),
      });
    }
  }

  cambiarTipo(tipo: TipoPregunta): void {
    this.formulario.controls.tipo.setValue(tipo);
    this.reiniciarOpciones(tipo);
  }

  agregarOpcion(): void {
    if (this.opciones.length < MAXIMO_OPCIONES) {
      this.opciones.push(this.crearOpcion(''));
    }
  }

  quitarOpcion(indice: number): void {
    if (this.opciones.length <= MINIMO_OPCIONES) {
      return;
    }
    this.opciones.removeAt(indice);
    const correcta = this.formulario.controls.correcta.value;
    if (correcta === indice) {
      this.formulario.controls.correcta.setValue(0);
    } else if (correcta > indice) {
      this.formulario.controls.correcta.setValue(correcta - 1);
    }
  }

  marcarCorrecta(indice: number): void {
    this.formulario.controls.correcta.setValue(indice);
  }

  guardar(): void {
    this.enviado.set(true);
    if (this.formulario.invalid) {
      return;
    }
    const solicitud = construirSolicitud(this.formulario.getRawValue() as ValorFormularioPregunta, this.activaOriginal);
    const id = this.preguntaNumerica();
    this.guardando.set(true);
    this.error.set(null);
    const accion = id === null ? this.preguntas.crear(solicitud) : this.preguntas.actualizar(id, solicitud);
    accion.subscribe({
      next: (pregunta) =>
        void this.router.navigate(['/courses', this.cursoNumerico(), 'topics', pregunta.temaId, 'questions']),
      error: (error: unknown) => {
        this.guardando.set(false);
        this.error.set(mensajeDeError(error, this.translate));
      },
    });
  }

  private cargarPregunta(pregunta: Pregunta): void {
    this.activaOriginal = pregunta.activa;
    this.opciones.clear();
    pregunta.opciones.forEach((opcion) => this.opciones.push(this.crearOpcion(opcion.texto)));
    this.formulario.patchValue({
      temaId: pregunta.temaId,
      dificultad: pregunta.dificultad,
      tipo: pregunta.tipo,
      enunciado: pregunta.enunciado,
      retroalimentacion: pregunta.retroalimentacion ?? '',
      correcta: Math.max(
        0,
        pregunta.opciones.findIndex((opcion) => opcion.esCorrecta),
      ),
    });
    this.detector.markForCheck();
  }

  private reiniciarOpciones(tipo: TipoPregunta): void {
    this.opciones.clear();
    opcionesIniciales(tipo).forEach((texto) => this.opciones.push(this.crearOpcion(texto)));
    this.formulario.controls.correcta.setValue(0);
  }

  private crearOpcion(texto: string): FormControl<string> {
    return this.fb.control(texto, [Validators.required, Validators.maxLength(500)]);
  }
}
