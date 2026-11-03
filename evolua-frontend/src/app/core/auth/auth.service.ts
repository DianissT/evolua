import { HttpClient } from '@angular/common/http';
import { Injectable, computed, inject, signal } from '@angular/core';
import { Router } from '@angular/router';
import { Observable, catchError, map, of, tap } from 'rxjs';

import { LoginRequest, LoginResponse, Rol, Usuario } from '../models/usuario.model';
import { tokenVigente } from './token.util';

interface SesionGuardada {
  token: string;
  usuario: Usuario;
}

/**
 * Gestiona la sesión del usuario: inicio y cierre de sesión, token JWT y datos del usuario autenticado.
 * La sesión se guarda en sessionStorage, por lo que se pierde al cerrar la pestaña.
 */
@Injectable({ providedIn: 'root' })
export class AuthService {
  private static readonly CLAVE = 'evolua.sesion';

  private readonly http = inject(HttpClient);
  private readonly router = inject(Router);
  private readonly tokenActual = signal<string | null>(null);

  readonly usuario = signal<Usuario | null>(null);
  readonly autenticado = computed(() => this.usuario() !== null && this.tokenActual() !== null);
  readonly rol = computed<Rol | null>(() => this.usuario()?.rol ?? null);

  token(): string | null {
    return this.tokenActual();
  }

  login(solicitud: LoginRequest): Observable<Usuario> {
    return this.http.post<LoginResponse>('/api/v1/auth/login', solicitud).pipe(
      tap((respuesta) => this.guardarSesion(respuesta.token, respuesta.usuario)),
      map((respuesta) => respuesta.usuario),
    );
  }

  cerrarSesion(): void {
    sessionStorage.removeItem(AuthService.CLAVE);
    this.tokenActual.set(null);
    this.usuario.set(null);
  }

  salir(): void {
    this.cerrarSesion();
    void this.router.navigate(['/login']);
  }

  /**
   * Recupera la sesión guardada y la valida con el servidor (GET /users/me). Si el token expiró o no es
   * válido, la sesión se descarta.
   */
  restaurarSesion(): Observable<unknown> {
    const guardada = this.leerSesion();
    if (guardada === null) {
      return of(null);
    }
    this.tokenActual.set(guardada.token);
    this.usuario.set(guardada.usuario);
    return this.http.get<Usuario>('/api/v1/users/me').pipe(
      tap((usuario) => this.guardarSesion(guardada.token, usuario)),
      catchError(() => {
        this.cerrarSesion();
        return of(null);
      }),
    );
  }

  private guardarSesion(token: string, usuario: Usuario): void {
    const sesion: SesionGuardada = { token, usuario };
    sessionStorage.setItem(AuthService.CLAVE, JSON.stringify(sesion));
    this.tokenActual.set(token);
    this.usuario.set(usuario);
  }

  private leerSesion(): SesionGuardada | null {
    const crudo = sessionStorage.getItem(AuthService.CLAVE);
    if (crudo === null) {
      return null;
    }
    try {
      const sesion = JSON.parse(crudo) as SesionGuardada;
      if (tokenVigente(sesion.token)) {
        return sesion;
      }
    } catch {
      sessionStorage.removeItem(AuthService.CLAVE);
      return null;
    }
    sessionStorage.removeItem(AuthService.CLAVE);
    return null;
  }
}
