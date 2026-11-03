export interface Curso {
  id: number;
  nombre: string;
  descripcion: string | null;
  docenteId: number;
  docenteNombre: string;
  activo: boolean;
  fechaCreacion: string;
}

export interface CursoRequest {
  nombre: string;
  descripcion: string | null;
  activo?: boolean;
}

export interface Tema {
  id: number;
  cursoId: number;
  nombre: string;
  orden: number;
  activo: boolean;
}

export interface TemaRequest {
  cursoId: number;
  nombre: string;
  orden?: number;
  activo?: boolean;
}
