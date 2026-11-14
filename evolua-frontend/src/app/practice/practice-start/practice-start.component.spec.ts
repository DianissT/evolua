import { HttpErrorResponse } from '@angular/common/http';
import { TestBed } from '@angular/core/testing';
import { Router, provideRouter } from '@angular/router';
import { provideTranslateService } from '@ngx-translate/core';
import { of, throwError } from 'rxjs';

import { AttemptService } from '../../core/services/attempt.service';
import { ProgressService } from '../../core/services/progress.service';
import { PracticeStartComponent } from './practice-start.component';

describe('PracticeStartComponent', () => {
  const iniciarPractica = vi.fn();

  function crear(temaId?: string) {
    const fixture = TestBed.createComponent(PracticeStartComponent);
    if (temaId !== undefined) {
      fixture.componentRef.setInput('temaId', temaId);
    }
    fixture.detectChanges();
    return fixture;
  }

  beforeEach(() => {
    iniciarPractica.mockReset().mockReturnValue(of({ id: 55 }));
    TestBed.configureTestingModule({
      imports: [PracticeStartComponent],
      providers: [
        provideRouter([]),
        provideTranslateService({ lang: 'es' }),
        {
          provide: ProgressService,
          useValue: {
            miProgreso: () =>
              of([
                {
                  temaId: 1, temaNombre: 'Ciclos', cursoId: 1, cursoNombre: 'Fundamentos', totalRespuestas: 5,
                  respuestasCorrectas: 2, porcentajeAciertos: 40, intentosSatisfactorios: 0, nivel: 'REFUERZO',
                  fechaActualizacion: null,
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
                  tipoActividad: 'REFUERZO', dificultadMinima: 1, dificultadMaxima: 1, motivo: 'Refuerza Ciclos.',
                  fechaCreacion: '2026-11-13T10:20:00',
                },
              ]),
          },
        },
        { provide: AttemptService, useValue: { iniciarPractica } },
      ],
    });
  });

  it('agrupa los temas por curso y preselecciona el tema de la ruta', () => {
    const fixture = crear('1');
    const elemento = fixture.nativeElement as HTMLElement;

    expect(elemento.querySelectorAll('optgroup').length).toBe(1);
    expect(fixture.componentInstance.form.controls.tema.value).toBe(1);
  });

  it('en modalidad automática explica la dificultad según la recomendación', () => {
    const fixture = crear('1');
    expect((fixture.nativeElement as HTMLElement).textContent).toContain('practice.autoWithRec');

    fixture.componentInstance.form.controls.tema.setValue(2);
    fixture.detectChanges();
    expect((fixture.nativeElement as HTMLElement).textContent).toContain('practice.autoWithoutRec');
  });

  it('inicia una práctica automática sin dificultad ni cantidad y abre el intento', () => {
    const navegar = vi.spyOn(TestBed.inject(Router), 'navigate').mockResolvedValue(true);
    const fixture = crear('1');

    fixture.componentInstance.iniciar();

    expect(iniciarPractica).toHaveBeenCalledWith(1, { modalidad: 'AUTOMATICA' });
    expect(navegar).toHaveBeenCalledWith(['/attempts', 55]);
  });

  it('en modalidad libre envía la dificultad y la cantidad elegidas', () => {
    vi.spyOn(TestBed.inject(Router), 'navigate').mockResolvedValue(true);
    const fixture = crear('2');
    fixture.componentInstance.form.patchValue({ modalidad: 'LIBRE', dificultad: 3, cantidad: 8 });
    fixture.detectChanges();

    fixture.componentInstance.iniciar();

    expect(iniciarPractica).toHaveBeenCalledWith(2, { modalidad: 'LIBRE', dificultad: 3, cantidad: 8 });
  });

  it('no inicia sin tema ni con una cantidad fuera de rango', () => {
    const fixture = crear();
    fixture.componentInstance.iniciar();
    expect(iniciarPractica).not.toHaveBeenCalled();

    fixture.componentInstance.form.patchValue({ tema: 1, modalidad: 'LIBRE', cantidad: 50 });
    fixture.componentInstance.iniciar();
    expect(iniciarPractica).not.toHaveBeenCalled();
  });

  it('muestra el mensaje del servidor si no se puede iniciar la práctica', () => {
    iniciarPractica.mockReturnValue(
      throwError(() => new HttpErrorResponse({ status: 409, error: { mensaje: 'El tema no tiene preguntas activas' } })),
    );
    const fixture = crear('1');

    fixture.componentInstance.iniciar();

    expect(fixture.componentInstance.error()).toBe('El tema no tiene preguntas activas');
    expect(fixture.componentInstance.enviando()).toBe(false);
  });
});
