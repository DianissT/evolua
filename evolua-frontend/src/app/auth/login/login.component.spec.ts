import { HttpErrorResponse } from '@angular/common/http';
import { signal } from '@angular/core';
import { TestBed } from '@angular/core/testing';
import { Router, provideRouter } from '@angular/router';
import { provideTranslateService } from '@ngx-translate/core';
import { of, throwError } from 'rxjs';

import { AuthService } from '../../core/auth/auth.service';
import { LanguageService } from '../../core/i18n/language.service';
import { LoginComponent } from './login.component';

describe('LoginComponent', () => {
  const login = vi.fn();

  beforeEach(async () => {
    login.mockReset();
    await TestBed.configureTestingModule({
      imports: [LoginComponent],
      providers: [
        provideRouter([]),
        provideTranslateService({ lang: 'es' }),
        { provide: AuthService, useValue: { login } },
        { provide: LanguageService, useValue: { idiomas: ['es', 'en'], actual: signal('es'), usar: () => of(null) } },
      ],
    }).compileComponents();
  });

  function enviar(correo: string, password: string) {
    const fixture = TestBed.createComponent(LoginComponent);
    fixture.detectChanges();
    const elemento = fixture.nativeElement as HTMLElement;
    (elemento.querySelector('#correo') as HTMLInputElement).value = correo;
    (elemento.querySelector('#correo') as HTMLInputElement).dispatchEvent(new Event('input'));
    (elemento.querySelector('#password') as HTMLInputElement).value = password;
    (elemento.querySelector('#password') as HTMLInputElement).dispatchEvent(new Event('input'));
    elemento.querySelector('form')?.dispatchEvent(new Event('submit'));
    fixture.detectChanges();
    return { fixture, elemento };
  }

  it('no envía el formulario vacío y muestra los errores', () => {
    const { elemento } = enviar('', '');
    expect(login).not.toHaveBeenCalled();
    expect(elemento.querySelectorAll('.invalid-feedback').length).toBe(2);
  });

  it('rechaza un correo con formato inválido', () => {
    const { elemento } = enviar('no-es-correo', 'clave');
    expect(login).not.toHaveBeenCalled();
    expect(elemento.querySelector('#correo-error')?.textContent).toContain('login.emailInvalid');
  });

  it('inicia sesión y redirige a la ruta de inicio del rol', () => {
    login.mockReturnValue(of({ id: 2, nombre: 'Laura', correo: 'laura@uni.edu', rol: 'DOCENTE' }));
    const navegar = vi.spyOn(TestBed.inject(Router), 'navigateByUrl').mockResolvedValue(true);
    enviar('laura@uni.edu', 'Evolua2026!');
    expect(login).toHaveBeenCalledWith({ correo: 'laura@uni.edu', password: 'Evolua2026!' });
    expect(navegar).toHaveBeenCalledWith('/courses');
  });

  it('muestra un mensaje cuando las credenciales son incorrectas', () => {
    login.mockReturnValue(throwError(() => new HttpErrorResponse({ status: 401 })));
    const { elemento } = enviar('laura@uni.edu', 'incorrecta');
    expect(elemento.querySelector('[role="alert"]')?.textContent).toContain('login.invalid');
  });
});
