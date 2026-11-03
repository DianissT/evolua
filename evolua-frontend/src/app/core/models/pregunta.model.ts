export type TipoPregunta = 'SELECCION_MULTIPLE' | 'VERDADERO_FALSO';

export interface Opcion {
  id: number;
  texto: string;
  esCorrecta: boolean;
  orden: number;
}

export interface Pregunta {
  id: number;
  temaId: number;
  enunciado: string;
  dificultad: number;
  tipo: TipoPregunta;
  retroalimentacion: string | null;
  activa: boolean;
  opciones: Opcion[];
}

export interface OpcionRequest {
  texto: string;
  esCorrecta: boolean;
}

export interface PreguntaRequest {
  temaId: number;
  enunciado: string;
  dificultad: number;
  tipo: TipoPregunta;
  retroalimentacion: string | null;
  activa?: boolean;
  opciones: OpcionRequest[];
}
