import { HttpErrorResponse } from '@angular/common/http';
import { TestBed } from '@angular/core/testing';
import { Router, provideRouter } from '@angular/router';
import { provideTranslateService } from '@ngx-translate/core';
import { of, throwError } from 'rxjs';

import { Intento } from '../../core/models/intento.model';
import { AttemptService } from '../../core/services/attempt.service';
import { AttemptComponent } from './attempt.component';

const intento: Intento = {
  id: 7,
  evaluacionId: 1,
  evaluacionNombre: 'Evaluación inicial',
  estado: 'EN_CURSO',
  fechaInicio: '2026-11-13T10:00:00',
  preguntas: [
    {
      id: 11,
      enunciado: '¿Qué imprime este código?\nSystem.out.println(1);',
      tipo: 'SELECCION_MULTIPLE',
      opciones: [
        { id: 111, texto: 'Uno' },
        { id: 112, texto: 'Dos' },
      ],
      opcionSeleccionadaId: null,
    },
    {
      id: 12,
      enunciado: 'Segunda pregunta',
      tipo: 'SELECCION_MULTIPLE',
      opciones: [
        { id: 121, texto: 'A' },
        { id: 122, texto: 'B' },
      ],
      opcionSeleccionadaId: null,
    },
    {
      id: 13,
      enunciado: 'Tercera pregunta',
      tipo: 'VERDADERO_FALSO',
      opciones: [
        { id: 131, texto: 'Verdadero' },
        { id: 132, texto: 'Falso' },
      ],
      opcionSeleccionadaId: 132,
    },
  ],
};

describe('AttemptComponent', () => {
  const obtener = vi.fn();
  const responder = vi.fn();
  const finalizar = vi.fn();

  function crear() {
    const fixture = TestBed.createComponent(AttemptComponent);
    fixture.componentRef.setInput('attemptId', '7');
    fixture.detectChanges();
    return fixture;
  }

  function boton(fixture: ReturnType<typeof crear>, texto: string): HTMLButtonElement {
    const botones = Array.from((fixture.nativeElement as HTMLElement).querySelectorAll('button'));
    return botones.find((b) => b.textContent?.trim() === texto) as HTMLButtonElement;
  }

  beforeEach(() => {
    obtener.mockReset().mockReturnValue(of(intento));
    responder.mockReset().mockReturnValue(of({}));
    finalizar.mockReset().mockReturnValue(of({}));
    vi.stubGlobal('confirm', vi.fn(() => true));
    TestBed.configureTestingModule({
      imports: [AttemptComponent],
      providers: [
        provideRouter([]),
        provideTranslateService({ lang: 'es' }),
        { provide: AttemptService, useValue: { obtener, responder, finalizar } },
      ],
    });
  });

  afterEach(() => vi.unstubAllGlobals());

  it('muestra la primera pregunta sin responder, con su código y sus opciones', () => {
    const elemento = crear().nativeElement as HTMLElement;
    expect(elemento.querySelector('h1')?.textContent).toContain('¿Qué imprime este código?');
    expect(elemento.querySelector('pre.codigo')?.textContent).toContain('System.out.println(1);');
    expect(elemento.querySelectorAll('input[type="radio"]').length).toBe(2);
  });

  it('guarda la respuesta al elegir una opción', () => {
    const fixture = crear();
    const radio = (fixture.nativeElement as HTMLElement).querySelector('input[type="radio"]') as HTMLInputElement;
    radio.dispatchEvent(new Event('change'));
    fixture.detectChanges();

    expect(responder).toHaveBeenCalledWith(7, { preguntaId: 11, opcionId: 111 });
    expect(fixture.componentInstance.respuestas()[11]).toBe(111);
  });

  it('si no se puede guardar, revierte la respuesta y muestra el error', () => {
    responder.mockReturnValue(throwError(() => new HttpErrorResponse({ status: 500 })));
    const fixture = crear();
    fixture.componentInstance.seleccionar(11, 112);

    expect(fixture.componentInstance.respuestas()[11]).toBeUndefined();
    expect(fixture.componentInstance.error()).toBe('errors.generic');
  });

  it('permite avanzar, retroceder y saltar entre preguntas', () => {
    const fixture = crear();
    boton(fixture, 'attempt.next').click();
    fixture.detectChanges();
    expect(fixture.componentInstance.indice()).toBe(1);
    boton(fixture, 'attempt.previous').click();
    fixture.detectChanges();
    expect(fixture.componentInstance.indice()).toBe(0);
    fixture.componentInstance.ir(99);
    expect(fixture.componentInstance.indice()).toBe(2);
  });

  it('pide confirmación si quedan preguntas sin responder y finaliza al confirmar', () => {
    const navegar = vi.spyOn(TestBed.inject(Router), 'navigate').mockResolvedValue(true);
    const fixture = crear();
    fixture.componentInstance.ir(2);
    fixture.detectChanges();

    boton(fixture, 'attempt.finish').click();

    expect(confirm).toHaveBeenCalledTimes(1);
    expect(finalizar).toHaveBeenCalledWith(7);
    expect(navegar).toHaveBeenCalledWith(['/attempts', 7, 'result']);
  });

  it('no finaliza si el estudiante cancela la confirmación', () => {
    vi.stubGlobal('confirm', vi.fn(() => false));
    const fixture = crear();
    fixture.componentInstance.ir(2);
    fixture.componentInstance.finalizar();
    expect(finalizar).not.toHaveBeenCalled();
  });

  it('si el intento ya finalizó, va directamente al resultado', () => {
    obtener.mockReturnValue(throwError(() => new HttpErrorResponse({ status: 409 })));
    const navegar = vi.spyOn(TestBed.inject(Router), 'navigate').mockResolvedValue(true);
    crear();
    expect(navegar).toHaveBeenCalledWith(['/attempts', 7, 'result']);
  });
});
