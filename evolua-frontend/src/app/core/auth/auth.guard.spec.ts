import { signal } from '@angular/core';
import { TestBed } from '@angular/core/testing';
import { ActivatedRouteSnapshot, Router, RouterStateSnapshot, UrlTree, provideRouter } from '@angular/router';

import { Rol } from '../models/usuario.model';
import { authGuard, guestGuard, rolGuard, rutaInicial } from './auth.guard';
import { AuthService } from './auth.service';

function simularAuth(rol: Rol | null) {
  const usuario = signal(rol === null ? null : { id: 1, nombre: 'X', correo: 'x@uni.edu', rol });
  return {
    autenticado: () => usuario() !== null,
    rol: () => usuario()?.rol ?? null,
  };
}

function ejecutar(guardia: ReturnType<typeof rolGuard>, url = '/courses') {
  const ruta = {} as ActivatedRouteSnapshot;
  const estado = { url } as RouterStateSnapshot;
  return TestBed.runInInjectionContext(() => guardia(ruta, estado));
}

describe('guards', () => {
  function configurar(rol: Rol | null) {
    TestBed.configureTestingModule({
      providers: [provideRouter([]), { provide: AuthService, useValue: simularAuth(rol) }],
    });
  }

  it('authGuard redirige al login a un usuario anónimo', () => {
    configurar(null);
    const resultado = ejecutar(authGuard, '/courses/5');
    expect(resultado instanceof UrlTree).toBe(true);
    expect(TestBed.inject(Router).serializeUrl(resultado as UrlTree)).toBe('/login?returnUrl=%2Fcourses%2F5');
  });

  it('authGuard permite a un usuario autenticado', () => {
    configurar('ESTUDIANTE');
    expect(ejecutar(authGuard)).toBe(true);
  });

  it('guestGuard redirige al inicio a un usuario autenticado', () => {
    configurar('DOCENTE');
    const resultado = ejecutar(guestGuard) as UrlTree;
    expect(TestBed.inject(Router).serializeUrl(resultado)).toBe('/courses');
  });

  it('rolGuard permite el rol indicado', () => {
    configurar('DOCENTE');
    expect(ejecutar(rolGuard('DOCENTE'))).toBe(true);
  });

  it('rolGuard envía a acceso denegado a otro rol', () => {
    configurar('ESTUDIANTE');
    const resultado = ejecutar(rolGuard('DOCENTE')) as UrlTree;
    expect(TestBed.inject(Router).serializeUrl(resultado)).toBe('/forbidden');
  });

  it('cada rol tiene su ruta de inicio', () => {
    expect(rutaInicial('ADMIN')).toBe('/admin/audit');
    expect(rutaInicial('DOCENTE')).toBe('/courses');
    expect(rutaInicial('ESTUDIANTE')).toBe('/courses');
    expect(rutaInicial(null)).toBe('/login');
  });
});
