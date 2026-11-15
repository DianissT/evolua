import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';

import { Configuracion, UsuarioAdmin, UsuarioNuevo } from '../models/admin.model';
import { Rol } from '../models/usuario.model';

/**
 * Acceso a los servicios REST de administración.
 */
@Injectable({ providedIn: 'root' })
export class AdminService {
  private readonly http = inject(HttpClient);
  private readonly usuarios = '/api/v1/admin/users';
  private readonly config = '/api/v1/admin/config';

  listarUsuarios(): Observable<UsuarioAdmin[]> {
    return this.http.get<UsuarioAdmin[]>(this.usuarios);
  }

  crearUsuario(solicitud: UsuarioNuevo): Observable<UsuarioAdmin> {
    return this.http.post<UsuarioAdmin>(this.usuarios, solicitud);
  }

  cambiarRol(id: number, rol: Rol): Observable<UsuarioAdmin> {
    return this.http.put<UsuarioAdmin>(`${this.usuarios}/${id}/role`, { rol });
  }

  cambiarEstado(id: number, activo: boolean): Observable<UsuarioAdmin> {
    return this.http.put<UsuarioAdmin>(`${this.usuarios}/${id}/active`, { activo });
  }

  listarConfiguracion(): Observable<Configuracion[]> {
    return this.http.get<Configuracion[]>(this.config);
  }

  actualizarConfiguracion(clave: string, valor: string): Observable<Configuracion> {
    return this.http.put<Configuracion>(`${this.config}/${encodeURIComponent(clave)}`, { valor });
  }
}
