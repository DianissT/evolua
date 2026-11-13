import { TestBed } from '@angular/core/testing';
import { provideTranslateService } from '@ngx-translate/core';
import { of } from 'rxjs';

import { EnrollmentService } from '../../core/services/enrollment.service';
import { EnrollmentPanelComponent } from './enrollment-panel.component';

describe('EnrollmentPanelComponent', () => {
  const listar = vi.fn();
  const matricular = vi.fn();
  const eliminar = vi.fn();
  const matricula = { id: 1, estudianteId: 4, estudianteNombre: 'Camila Rojas', estudianteCorreo: 'camila@uni.edu', cursoId: 3, fechaMatricula: '2026-11-13T10:00:00' };

  function montar() {
    TestBed.configureTestingModule({
      imports: [EnrollmentPanelComponent],
      providers: [provideTranslateService({ lang: 'es' }), { provide: EnrollmentService, useValue: { listar, matricular, eliminar } }],
    });
    const fixture = TestBed.createComponent(EnrollmentPanelComponent);
    fixture.componentRef.setInput('courseId', 3);
    fixture.detectChanges();
    return fixture;
  }

  beforeEach(() => {
    listar.mockReset().mockReturnValue(of([matricula]));
    matricular.mockReset().mockReturnValue(of(matricula));
    eliminar.mockReset().mockReturnValue(of(undefined));
    vi.stubGlobal('confirm', vi.fn(() => true));
  });

  afterEach(() => vi.unstubAllGlobals());

  it('lista los estudiantes matriculados', () => {
    const fixture = montar();
    expect((fixture.nativeElement as HTMLElement).textContent).toContain('camila@uni.edu');
  });

  it('no envía un correo inválido', () => {
    const fixture = montar();
    fixture.componentInstance.correo.setValue('no-es-correo');
    fixture.componentInstance.matricular();
    expect(matricular).not.toHaveBeenCalled();
  });

  it('matricula al estudiante, limpia el campo y recarga la lista', () => {
    const fixture = montar();
    fixture.componentInstance.correo.setValue('  camila@uni.edu ');

    fixture.componentInstance.matricular();

    expect(matricular).toHaveBeenCalledWith(3, 'camila@uni.edu');
    expect(fixture.componentInstance.correo.value).toBe('');
    expect(fixture.componentInstance.exito()).toBe('enrollments.added');
    expect(listar).toHaveBeenCalledTimes(2);
  });

  it('quita una matrícula tras confirmar', () => {
    const fixture = montar();
    fixture.componentInstance.quitar(matricula);
    expect(eliminar).toHaveBeenCalledWith(3, 4);
  });
});
