import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';

import { LoginResponse } from '../models/usuario.model';
import { AuthService } from './auth.service';

function crearToken(exp: number): string {
  const codificar = (objeto: object) => btoa(JSON.stringify(objeto)).replace(/=/g, '');
  return `${codificar({ alg: 'HS256' })}.${codificar({ exp })}.firma`;
}

const usuario = { id: 3, nombre: 'Ana Pérez', correo: 'ana.perez@uni.edu', rol: 'ESTUDIANTE' as const };

describe('AuthService', () => {
  let http: HttpTestingController;

  beforeEach(() => {
    sessionStorage.clear();
    TestBed.configureTestingModule({
      providers: [provideRouter([]), provideHttpClient(), provideHttpClientTesting()],
    });
    http = TestBed.inject(HttpTestingController);
  });

  afterEach(() => http.verify());

  it('guarda el token y el usuario al iniciar sesión', () => {
    const auth = TestBed.inject(AuthService);
    const token = crearToken(Math.floor(Date.now() / 1000) + 600);
    auth.login({ correo: usuario.correo, password: 'x' }).subscribe();

    const solicitud = http.expectOne('/api/v1/auth/login');
    expect(solicitud.request.method).toBe('POST');
    const respuesta: LoginResponse = { token, tipo: 'Bearer', expiraEnSegundos: 1800, usuario };
    solicitud.flush(respuesta);

    expect(auth.autenticado()).toBe(true);
    expect(auth.rol()).toBe('ESTUDIANTE');
    expect(auth.token()).toBe(token);
  });

  it('cierra la sesión y limpia el almacenamiento', () => {
    const auth = TestBed.inject(AuthService);
    auth.login({ correo: usuario.correo, password: 'x' }).subscribe();
    http
      .expectOne('/api/v1/auth/login')
      .flush({ token: crearToken(Math.floor(Date.now() / 1000) + 600), tipo: 'Bearer', expiraEnSegundos: 1800, usuario });

    auth.cerrarSesion();

    expect(auth.autenticado()).toBe(false);
    expect(auth.token()).toBeNull();
    expect(sessionStorage.getItem('evolua.sesion')).toBeNull();
  });

  it('restaura la sesión guardada y la valida con el servidor', () => {
    const token = crearToken(Math.floor(Date.now() / 1000) + 600);
    sessionStorage.setItem('evolua.sesion', JSON.stringify({ token, usuario }));
    const auth = TestBed.inject(AuthService);

    auth.restaurarSesion().subscribe();
    http.expectOne('/api/v1/users/me').flush(usuario);

    expect(auth.autenticado()).toBe(true);
  });

  it('descarta una sesión guardada con el token expirado', () => {
    const token = crearToken(Math.floor(Date.now() / 1000) - 10);
    sessionStorage.setItem('evolua.sesion', JSON.stringify({ token, usuario }));
    const auth = TestBed.inject(AuthService);

    auth.restaurarSesion().subscribe();

    expect(auth.autenticado()).toBe(false);
    expect(sessionStorage.getItem('evolua.sesion')).toBeNull();
  });

  it('descarta la sesión si el servidor rechaza el token', () => {
    const token = crearToken(Math.floor(Date.now() / 1000) + 600);
    sessionStorage.setItem('evolua.sesion', JSON.stringify({ token, usuario }));
    const auth = TestBed.inject(AuthService);

    auth.restaurarSesion().subscribe();
    http.expectOne('/api/v1/users/me').flush({}, { status: 401, statusText: 'Unauthorized' });

    expect(auth.autenticado()).toBe(false);
  });
});
