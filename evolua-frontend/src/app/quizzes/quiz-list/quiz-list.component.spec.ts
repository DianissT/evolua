import { TestBed } from '@angular/core/testing';
import { Router, provideRouter } from '@angular/router';
import { provideTranslateService } from '@ngx-translate/core';
import { of } from 'rxjs';

import { AttemptService } from '../../core/services/attempt.service';
import { QuizService } from '../../core/services/quiz.service';
import { QuizListComponent } from './quiz-list.component';

const quizzes = [
  { id: 1, nombre: 'Inicial', tipo: 'INICIAL', intentosPermitidos: null, activa: true, totalPreguntas: 15, intentosUsados: 0 },
  { id: 2, nombre: 'Límite', tipo: 'FORMATIVA', intentosPermitidos: 1, activa: true, totalPreguntas: 5, intentosUsados: 1 },
];

describe('QuizListComponent', () => {
  const iniciar = vi.fn();
  const historial = vi.fn();

  function montar(esDocente: boolean) {
    const fixture = TestBed.createComponent(QuizListComponent);
    fixture.componentRef.setInput('courseId', 3);
    fixture.componentRef.setInput('esDocente', esDocente);
    fixture.detectChanges();
    return fixture;
  }

  beforeEach(() => {
    iniciar.mockReset().mockReturnValue(of({ id: 55 }));
    historial.mockReset().mockReturnValue(of([]));
    TestBed.configureTestingModule({
      imports: [QuizListComponent],
      providers: [
        provideRouter([]),
        provideTranslateService({ lang: 'es' }),
        { provide: QuizService, useValue: { listarPorCurso: () => of(quizzes) } },
        { provide: AttemptService, useValue: { iniciar, historial } },
      ],
    });
  });

  it('el estudiante inicia una evaluación y pasa a responderla', () => {
    const navegar = vi.spyOn(TestBed.inject(Router), 'navigate').mockResolvedValue(true);
    const fixture = montar(false);
    const botones = (fixture.nativeElement as HTMLElement).querySelectorAll<HTMLButtonElement>('button.btn-primary');

    botones[0].click();

    expect(iniciar).toHaveBeenCalledWith(1);
    expect(navegar).toHaveBeenCalledWith(['/attempts', 55]);
  });

  it('desactiva el botón cuando se alcanzó el límite de intentos', () => {
    const botones = (montar(false).nativeElement as HTMLElement).querySelectorAll<HTMLButtonElement>('button.btn-primary');
    expect(botones[0].disabled).toBe(false);
    expect(botones[1].disabled).toBe(true);
  });

  it('permite continuar un intento en curso aunque se haya usado el límite', () => {
    historial.mockReturnValue(of([{ id: 9, tipo: 'EVALUACION', evaluacionId: 2, evaluacionNombre: 'Límite', estado: 'EN_CURSO', fechaInicio: '2026-11-13T10:00:00', fechaFin: null, puntuacion: null }]));
    const botones = (montar(false).nativeElement as HTMLElement).querySelectorAll<HTMLButtonElement>('button.btn-primary');
    expect(botones[1].disabled).toBe(false);
    expect(botones[1].textContent?.trim()).toBe('quizzes.continue');
  });

  it('el docente ve las acciones de administración y no el botón de iniciar', () => {
    const elemento = montar(true).nativeElement as HTMLElement;
    expect(elemento.querySelector('a[href="/courses/3/quizzes/new"]')).not.toBeNull();
    expect(elemento.querySelector('a[href="/courses/3/quizzes/1/edit"]')).not.toBeNull();
    expect(elemento.querySelectorAll('button.btn-primary').length).toBe(0);
  });
});
