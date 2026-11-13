import { TipoPregunta } from './pregunta.model';

export type TipoEvaluacion = 'INICIAL' | 'FORMATIVA' | 'CONFIRMACION';

export interface QuizResumen {
  id: number;
  nombre: string;
  tipo: TipoEvaluacion;
  intentosPermitidos: number | null;
  activa: boolean;
  totalPreguntas: number;
  intentosUsados: number | null;
}

export interface QuizPregunta {
  id: number;
  enunciado: string;
  dificultad: number;
  tipo: TipoPregunta;
  temaId: number;
  orden: number;
}

export interface Quiz {
  id: number;
  cursoId: number;
  nombre: string;
  tipo: TipoEvaluacion;
  intentosPermitidos: number | null;
  activa: boolean;
  totalPreguntas: number;
  preguntas: QuizPregunta[];
}

export interface QuizRequest {
  cursoId: number;
  nombre: string;
  tipo: TipoEvaluacion;
  intentosPermitidos: number | null;
  activa?: boolean;
  preguntaIds: number[];
}
