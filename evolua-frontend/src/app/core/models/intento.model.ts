import { TipoPregunta } from './pregunta.model';
import { Recomendacion } from './progreso.model';

export type EstadoIntento = 'EN_CURSO' | 'FINALIZADO';

export interface OpcionIntento {
  id: number;
  texto: string;
}

export interface PreguntaIntento {
  id: number;
  enunciado: string;
  tipo: TipoPregunta;
  opciones: OpcionIntento[];
  opcionSeleccionadaId: number | null;
}

export interface Intento {
  id: number;
  evaluacionId: number;
  evaluacionNombre: string;
  estado: EstadoIntento;
  fechaInicio: string;
  preguntas: PreguntaIntento[];
}

export interface RespuestaSolicitud {
  preguntaId: number;
  opcionId: number;
}

export interface OpcionRevision {
  id: number;
  texto: string;
  esCorrecta: boolean;
}

export interface RevisionPregunta {
  id: number;
  enunciado: string;
  tipo: TipoPregunta;
  opciones: OpcionRevision[];
  opcionSeleccionadaId: number | null;
  esCorrecta: boolean;
  retroalimentacion: string | null;
}

export interface ResultadoTema {
  temaId: number;
  temaNombre: string;
  correctas: number;
  total: number;
  porcentaje: number;
}

export interface ResultadoIntento {
  id: number;
  evaluacionId: number;
  evaluacionNombre: string;
  estado: EstadoIntento;
  fechaInicio: string;
  fechaFin: string | null;
  puntuacion: number;
  totalPreguntas: number;
  correctas: number;
  incorrectas: number;
  sinResponder: number;
  preguntas: RevisionPregunta[];
  temas: ResultadoTema[];
  recomendaciones: Recomendacion[];
}

export interface IntentoResumen {
  id: number;
  tipo: 'EVALUACION' | 'PRACTICA';
  evaluacionId: number | null;
  evaluacionNombre: string | null;
  estado: EstadoIntento;
  fechaInicio: string;
  fechaFin: string | null;
  puntuacion: number | null;
}
