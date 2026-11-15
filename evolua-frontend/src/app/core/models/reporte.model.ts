export interface RendimientoTema {
  temaId: number;
  temaNombre: string;
  totalRespuestas: number;
  respuestasCorrectas: number;
  porcentajeAciertos: number;
  estudiantesEnRefuerzo: number;
}

export interface RendimientoEstudiante {
  estudianteId: number;
  nombre: string;
  correo: string;
  intentosFinalizados: number;
  promedioPuntuacion: number | null;
  ultimaActividad: string | null;
  temasEnRefuerzo: number;
}

export interface RendimientoCurso {
  cursoId: number;
  cursoNombre: string;
  estudiantesMatriculados: number;
  estudiantesConActividad: number;
  intentosFinalizados: number;
  promedioPuntuacion: number | null;
  temas: RendimientoTema[];
  estudiantes: RendimientoEstudiante[];
}

export interface PreguntaDificil {
  preguntaId: number;
  enunciado: string;
  temaId: number;
  temaNombre: string;
  dificultad: number;
  totalRespuestas: number;
  respuestasCorrectas: number;
  porcentajeAciertos: number;
}
