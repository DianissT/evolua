export type NivelDominio = 'SIN_DATOS' | 'REFUERZO' | 'INTERMEDIO' | 'AVANZADO' | 'DOMINADO';
export type TipoActividad = 'REFUERZO' | 'PRACTICA_ADICIONAL' | 'AVANZADO' | 'CONFIRMACION';

export interface ProgresoTema {
  temaId: number;
  temaNombre: string;
  cursoId: number;
  cursoNombre: string;
  totalRespuestas: number;
  respuestasCorrectas: number;
  porcentajeAciertos: number;
  intentosSatisfactorios: number;
  nivel: NivelDominio;
  fechaActualizacion: string | null;
}

export interface Recomendacion {
  id: number;
  temaId: number;
  temaNombre: string;
  cursoId: number;
  cursoNombre: string;
  tipoActividad: TipoActividad;
  dificultadMinima: number;
  dificultadMaxima: number;
  motivo: string;
  fechaCreacion: string;
}
