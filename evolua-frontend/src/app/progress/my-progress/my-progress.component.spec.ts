import { signal } from '@angular/core';
import { TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';
import { provideTranslateService } from '@ngx-translate/core';
import { of } from 'rxjs';

import { LanguageService } from '../../core/i18n/language.service';
import { AttemptService } from '../../core/services/attempt.service';
import { ProgressService } from '../../core/services/progress.service';
import { MyProgressComponent } from './my-progress.component';

describe('MyProgressComponent', () => {
  function crear() {
    TestBed.configureTestingModule({
      imports: [MyProgressComponent],
      providers: [
        provideRouter([]),
        provideTranslateService({ lang: 'es' }),
        { provide: LanguageService, useValue: { actual: signal('es') } },
        {
          provide: ProgressService,
          useValue: {
            miProgreso: () =>
              of([
                {
                  temaId: 1, temaNombre: 'Ciclos', cursoId: 1, cursoNombre: 'Fundamentos', totalRespuestas: 5,
                  respuestasCorrectas: 2, porcentajeAciertos: 40, intentosSatisfactorios: 0, nivel: 'REFUERZO',
                  fechaActualizacion: '2026-11-13T10:20:00',
                },
                {
                  temaId: 2, temaNombre: 'Variables', cursoId: 1, cursoNombre: 'Fundamentos', totalRespuestas: 0,
                  respuestasCorrectas: 0, porcentajeAciertos: 0, intentosSatisfactorios: 0, nivel: 'SIN_DATOS',
                  fechaActualizacion: null,
                },
              ]),
            misRecomendaciones: () =>
              of([
                {
                  id: 1, temaId: 1, temaNombre: 'Ciclos', cursoId: 1, cursoNombre: 'Fundamentos',
                  tipoActividad: 'REFUERZO', dificultadMinima: 1, dificultadMaxima: 1,
                  motivo: 'Acertaste 2 de 5 preguntas de Ciclos.', fechaCreacion: '2026-11-13T10:20:00',
                },
              ]),
          },
        },
        {
          provide: AttemptService,
          useValue: {
            historial: () =>
              of([
                { id: 9, tipo: 'EVALUACION', evaluacionId: 1, evaluacionNombre: 'Inicial', estado: 'EN_CURSO', fechaInicio: '2026-11-14T09:00:00', fechaFin: null, puntuacion: null },
                { id: 8, tipo: 'EVALUACION', evaluacionId: 1, evaluacionNombre: 'Inicial', estado: 'FINALIZADO', fechaInicio: '2026-11-13T10:00:00', fechaFin: '2026-11-13T10:20:00', puntuacion: 40 },
                { id: 7, tipo: 'EVALUACION', evaluacionId: 1, evaluacionNombre: 'Inicial', estado: 'FINALIZADO', fechaInicio: '2026-11-12T10:00:00', fechaFin: '2026-11-12T10:20:00', puntuacion: 100 },
              ]),
          },
        },
      ],
    });
    const fixture = TestBed.createComponent(MyProgressComponent);
    fixture.detectChanges();
    return fixture.nativeElement as HTMLElement;
  }

  it('muestra una tarjeta por tema con su nivel y su porcentaje', () => {
    const elemento = crear();
    expect(elemento.querySelectorAll('article.card').length).toBe(2);
    expect(elemento.querySelectorAll('.badge.text-bg-warning').length).toBeGreaterThanOrEqual(1);
    expect(elemento.textContent).toContain('40 %');
    expect(elemento.textContent).toContain('level.SIN_DATOS');
  });

  it('muestra las recomendaciones vigentes con su motivo', () => {
    expect(crear().textContent).toContain('Acertaste 2 de 5 preguntas de Ciclos.');
  });

  it('el historial enlaza al resultado o permite continuar un intento en curso', () => {
    const elemento = crear();
    const enlaces = Array.from(elemento.querySelectorAll('tbody a')).map((a) => a.getAttribute('href'));
    expect(enlaces).toEqual(['/attempts/9', '/attempts/8/result', '/attempts/7/result']);
  });

  it('la evolución muestra solo los intentos finalizados, del más antiguo al más reciente', () => {
    const barras = crear().querySelectorAll('.rounded-top');
    expect(barras.length).toBe(2);
  });
});
