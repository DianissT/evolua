import { signal } from '@angular/core';
import { TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';
import { provideTranslateService } from '@ngx-translate/core';
import { of } from 'rxjs';

import { LanguageService } from '../../core/i18n/language.service';
import { ResultadoIntento } from '../../core/models/intento.model';
import { AttemptService } from '../../core/services/attempt.service';
import { AttemptResultComponent } from './attempt-result.component';

const resultado: ResultadoIntento = {
  id: 7,
  evaluacionId: 1,
  evaluacionNombre: 'Evaluación inicial',
  estado: 'FINALIZADO',
  fechaInicio: '2026-11-13T10:00:00',
  fechaFin: '2026-11-13T10:20:00',
  puntuacion: 66.67,
  totalPreguntas: 3,
  correctas: 2,
  incorrectas: 1,
  sinResponder: 0,
  preguntas: [
    {
      id: 11,
      enunciado: '¿Qué imprime este código?\nSystem.out.println(1);',
      tipo: 'SELECCION_MULTIPLE',
      opciones: [
        { id: 111, texto: 'Uno', esCorrecta: true },
        { id: 112, texto: 'Dos', esCorrecta: false },
      ],
      opcionSeleccionadaId: 111,
      esCorrecta: true,
      retroalimentacion: 'Imprime 1.',
    },
    {
      id: 12,
      enunciado: 'Segunda pregunta',
      tipo: 'SELECCION_MULTIPLE',
      opciones: [
        { id: 121, texto: 'A', esCorrecta: true },
        { id: 122, texto: 'B', esCorrecta: false },
      ],
      opcionSeleccionadaId: 122,
      esCorrecta: false,
      retroalimentacion: null,
    },
  ],
  temas: [
    { temaId: 1, temaNombre: 'Ciclos', correctas: 1, total: 3, porcentaje: 33.33 },
    { temaId: 2, temaNombre: 'Variables', correctas: 5, total: 5, porcentaje: 100 },
  ],
  recomendaciones: [
    {
      id: 1,
      temaId: 1,
      temaNombre: 'Ciclos',
      cursoId: 1,
      cursoNombre: 'Fundamentos',
      tipoActividad: 'REFUERZO',
      dificultadMinima: 1,
      dificultadMaxima: 1,
      motivo: 'Acertaste 1 de 3 preguntas de Ciclos.',
      fechaCreacion: '2026-11-13T10:20:00',
    },
  ],
};

describe('AttemptResultComponent', () => {
  function crear() {
    TestBed.configureTestingModule({
      imports: [AttemptResultComponent],
      providers: [
        provideRouter([]),
        provideTranslateService({ lang: 'es' }),
        { provide: AttemptService, useValue: { resultado: () => of(resultado) } },
        { provide: LanguageService, useValue: { actual: signal('es') } },
      ],
    });
    const fixture = TestBed.createComponent(AttemptResultComponent);
    fixture.componentRef.setInput('attemptId', '7');
    fixture.detectChanges();
    return fixture.nativeElement as HTMLElement;
  }

  it('muestra la puntuación calculada por el servidor con el formato del idioma', () => {
    expect(crear().textContent).toContain('66,67 %');
  });

  it('muestra el desempeño por tema y colorea la barra según la recomendación', () => {
    const elemento = crear();
    expect(elemento.textContent).toContain('Ciclos');
    expect(elemento.textContent).toContain('Variables');
    expect(elemento.querySelectorAll('.progress-bar.bg-warning').length).toBe(1);
    expect(elemento.querySelectorAll('.progress-bar.bg-secondary').length).toBe(1);
  });

  it('muestra la recomendación con su motivo y la dificultad sugerida', () => {
    const elemento = crear();
    expect(elemento.textContent).toContain('Acertaste 1 de 3 preguntas de Ciclos.');
    expect(elemento.textContent).toContain('activity.REFUERZO');
    expect(elemento.textContent).toContain('difficulty.1');
  });

  it('permite revisar cada pregunta con la respuesta correcta y la retroalimentación', () => {
    const elemento = crear();
    expect(elemento.querySelectorAll('details').length).toBe(2);
    expect(elemento.querySelector('details pre.codigo')?.textContent).toContain('System.out.println(1);');
    expect(elemento.textContent).toContain('Imprime 1.');
    expect(elemento.querySelectorAll('.badge.text-bg-success').length).toBeGreaterThanOrEqual(2);
  });
});
