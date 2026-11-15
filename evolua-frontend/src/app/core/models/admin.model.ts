import { Rol } from './usuario.model';

export interface UsuarioAdmin {
  id: number;
  nombre: string;
  correo: string;
  rol: Rol;
  activo: boolean;
  fechaCreacion: string;
}

export interface UsuarioNuevo {
  nombre: string;
  correo: string;
  password: string;
  rol: Rol;
}

export interface Configuracion {
  clave: string;
  valor: string;
  descripcion: string | null;
}
