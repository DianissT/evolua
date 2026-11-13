import { TestBed } from '@angular/core/testing';
import { Router, provideRouter } from '@angular/router';
import { provideTranslateService } from '@ngx-translate/core';
import { of } from 'rxjs';

import { CourseService } from '../../core/services/course.service';
import { QuizService } from '../../core/services/quiz.service';
import { TopicService } from '../../core/services/topic.service';
import { QuizFormComponent } from './quiz-form.component';

const pregunta = (id: number, temaId: number, activa = true) => ({
  id, temaId, enunciado: `Pregunta ${id}`, dificultad: 1, tipo: 'SELECCION_MULTIPLE', retroalimentacion: null, activa, opciones: [],
});

describe('QuizFormComponent', () => {
  const crear = vi.fn();
  const actualizar = vi.fn();
  const obtener = vi.fn();

  function montar(quizId?: string) {
    TestBed.configureTestingModule({
      imports: [QuizFormComponent],
      providers: [
        provideRouter([]),
        provideTranslateService({ lang: 'es' }),
        { provide: CourseService, useValue: { temas: () => of([{ id: 1, cursoId: 3, nombre: 'Variables', orden: 1, activo: true }, { id: 2, cursoId: 3, nombre: 'Ciclos', orden: 2, activo: true }]) } },
        { provide: TopicService, useValue: { preguntas: (id: number) => of(id === 1 ? [pregunta(10, 1), pregunta(11, 1)] : [pregunta(20, 2)]) } },
        { provide: QuizService, useValue: { crear, actualizar, obtener } },
      ],
    });
    const fixture = TestBed.createComponent(QuizFormComponent);
    fixture.componentRef.setInput('courseId', '3');
    if (quizId) {
      fixture.componentRef.setInput('quizId', quizId);
    }
    fixture.detectChanges();
    return fixture;
  }

  beforeEach(() => {
    crear.mockReset().mockReturnValue(of({}));
    actualizar.mockReset().mockReturnValue(of({}));
    obtener.mockReset().mockReturnValue(
      of({ id: 5, cursoId: 3, nombre: 'Original', tipo: 'INICIAL', intentosPermitidos: 2, activa: true, totalPreguntas: 1,
        preguntas: [{ id: 11, enunciado: 'Pregunta 11', dificultad: 1, tipo: 'SELECCION_MULTIPLE', temaId: 1, orden: 1 }] }),
    );
    vi.spyOn(Router.prototype, 'navigate').mockResolvedValue(true);
  });

  it('muestra las preguntas de todos los temas del curso', () => {
    const fixture = montar();
    expect((fixture.nativeElement as HTMLElement).querySelectorAll('input[type="checkbox"]').length).toBe(3);
  });

  it('no guarda una evaluación sin preguntas y muestra el aviso', () => {
    const fixture = montar();
    fixture.componentInstance.formulario.controls.nombre.setValue('Evaluación');
    fixture.componentInstance.guardar();
    fixture.detectChanges();
    expect(crear).not.toHaveBeenCalled();
    expect((fixture.nativeElement as HTMLElement).querySelector('[role="alert"]')?.textContent).toContain('quizzes.questionsRequired');
  });

  it('envía las preguntas en el orden en que se muestran, sin importar el orden de los clics', () => {
    const fixture = montar();
    const componente = fixture.componentInstance;
    componente.formulario.controls.nombre.setValue('  Evaluación  ');
    componente.alternar(20, true);
    componente.alternar(10, true);

    componente.guardar();

    expect(crear).toHaveBeenCalledWith({
      cursoId: 3,
      nombre: 'Evaluación',
      tipo: 'FORMATIVA',
      intentosPermitidos: null,
      preguntaIds: [10, 20],
    });
  });

  it('al editar carga los datos y actualiza conservando el estado de la evaluación', () => {
    const fixture = montar('5');
    const componente = fixture.componentInstance;
    expect(componente.formulario.controls.nombre.value).toBe('Original');
    expect(componente.seleccion()).toEqual([11]);

    componente.guardar();

    expect(actualizar).toHaveBeenCalledWith(5, {
      cursoId: 3, nombre: 'Original', tipo: 'INICIAL', intentosPermitidos: 2, activa: true, preguntaIds: [11],
    });
  });
});
