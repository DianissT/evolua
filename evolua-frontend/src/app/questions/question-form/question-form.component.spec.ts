import { TestBed } from '@angular/core/testing';
import { Router, provideRouter } from '@angular/router';
import { provideTranslateService } from '@ngx-translate/core';
import { Subject, of } from 'rxjs';

import { CourseService } from '../../core/services/course.service';
import { QuestionService } from '../../core/services/question.service';
import { QuestionFormComponent } from './question-form.component';

describe('QuestionFormComponent', () => {
  const crear = vi.fn();

  beforeEach(async () => {
    crear.mockReset();
    crear.mockReturnValue(of({ id: 9, temaId: 5 }));
    await TestBed.configureTestingModule({
      imports: [QuestionFormComponent],
      providers: [
        provideRouter([]),
        provideTranslateService({ lang: 'es' }),
        { provide: CourseService, useValue: { temas: () => of([{ id: 5, cursoId: 1, nombre: 'Ciclos', orden: 1, activo: true }]) } },
        { provide: QuestionService, useValue: { crear } },
      ],
    }).compileComponents();
  });

  function crearComponente() {
    const fixture = TestBed.createComponent(QuestionFormComponent);
    fixture.componentRef.setInput('courseId', '1');
    fixture.componentRef.setInput('topicId', '5');
    fixture.detectChanges();
    return fixture;
  }

  it('comienza con cuatro opciones de selección múltiple', () => {
    expect(crearComponente().componentInstance.opciones.length).toBe(4);
  });

  it('al elegir verdadero o falso quedan dos opciones de solo lectura', () => {
    const fixture = crearComponente();
    const radio = (fixture.nativeElement as HTMLElement).querySelector<HTMLInputElement>('#tipo-VERDADERO_FALSO');
    radio?.dispatchEvent(new Event('change'));
    fixture.detectChanges();
    const campos = (fixture.nativeElement as HTMLElement).querySelectorAll<HTMLInputElement>('input[type="text"]');
    expect(campos.length).toBe(2);
    expect(Array.from(campos).every((campo) => campo.readOnly)).toBe(true);
    expect(fixture.componentInstance.opciones.value).toEqual(['Verdadero', 'Falso']);
  });

  it('respeta el mínimo y el máximo de opciones', () => {
    const componente = crearComponente().componentInstance;
    for (let i = 0; i < 10; i++) {
      componente.agregarOpcion();
    }
    expect(componente.opciones.length).toBe(6);
    for (let i = 0; i < 10; i++) {
      componente.quitarOpcion(0);
    }
    expect(componente.opciones.length).toBe(2);
  });

  it('no envía la pregunta si hay opciones vacías', () => {
    const fixture = crearComponente();
    fixture.componentInstance.formulario.controls.enunciado.setValue('¿Pregunta?');
    fixture.componentInstance.guardar();
    expect(crear).not.toHaveBeenCalled();
  });

  it('envía la solicitud con la opción correcta elegida y vuelve a la lista', () => {
    const navegar = vi.spyOn(TestBed.inject(Router), 'navigate').mockResolvedValue(true);
    const fixture = crearComponente();
    const componente = fixture.componentInstance;
    componente.formulario.controls.enunciado.setValue('¿Cuánto es 2 + 2?');
    componente.opciones.setValue(['3', '4', '5', '6']);
    componente.marcarCorrecta(1);
    componente.formulario.controls.dificultad.setValue(2);

    componente.guardar();

    expect(crear).toHaveBeenCalledWith({
      temaId: 5,
      enunciado: '¿Cuánto es 2 + 2?',
      dificultad: 2,
      tipo: 'SELECCION_MULTIPLE',
      retroalimentacion: null,
      opciones: [
        { texto: '3', esCorrecta: false },
        { texto: '4', esCorrecta: true },
        { texto: '5', esCorrecta: false },
        { texto: '6', esCorrecta: false },
      ],
    });
    expect(navegar).toHaveBeenCalledWith(['/courses', 1, 'topics', 5, 'questions']);
  });

  it('al cargar una pregunta existente muestra sus opciones sin esperar otra interacción', async () => {
    const respuesta = new Subject<unknown>();
    TestBed.resetTestingModule();
    await TestBed.configureTestingModule({
      imports: [QuestionFormComponent],
      providers: [
        provideRouter([]),
        provideTranslateService({ lang: 'es' }),
        { provide: CourseService, useValue: { temas: () => of([{ id: 5, cursoId: 1, nombre: 'Ciclos', orden: 1, activo: true }]) } },
        { provide: QuestionService, useValue: { crear, obtener: () => respuesta } },
      ],
    }).compileComponents();
    const fixture = TestBed.createComponent(QuestionFormComponent);
    fixture.componentRef.setInput('courseId', '1');
    fixture.componentRef.setInput('topicId', '5');
    fixture.componentRef.setInput('questionId', '9');
    fixture.detectChanges();

    respuesta.next({
      id: 9,
      temaId: 5,
      enunciado: 'Un boolean admite true o false',
      dificultad: 1,
      tipo: 'VERDADERO_FALSO',
      retroalimentacion: null,
      activa: true,
      opciones: [
        { id: 1, texto: 'Verdadero', esCorrecta: true, orden: 1 },
        { id: 2, texto: 'Falso', esCorrecta: false, orden: 2 },
      ],
    });
    await fixture.whenStable();

    const campos = (fixture.nativeElement as HTMLElement).querySelectorAll<HTMLInputElement>('input[type="text"]');
    expect(campos.length).toBe(2);
    expect(campos[0].value).toBe('Verdadero');
  });
});
