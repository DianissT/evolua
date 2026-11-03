import { PreguntaRequest, TipoPregunta } from '../../core/models/pregunta.model';

export const TEXTO_VERDADERO = 'Verdadero';
export const TEXTO_FALSO = 'Falso';
export const MINIMO_OPCIONES = 2;
export const MAXIMO_OPCIONES = 6;
export const OPCIONES_INICIALES = 4;

export interface ValorFormularioPregunta {
  temaId: number;
  dificultad: number;
  tipo: TipoPregunta;
  enunciado: string;
  retroalimentacion: string;
  correcta: number;
  opciones: string[];
}

/**
 * Textos iniciales de las opciones según el tipo de pregunta.
 */
export function opcionesIniciales(tipo: TipoPregunta): string[] {
  return tipo === 'VERDADERO_FALSO'
    ? [TEXTO_VERDADERO, TEXTO_FALSO]
    : Array.from({ length: OPCIONES_INICIALES }, () => '');
}

/**
 * Convierte el valor del formulario en la solicitud que espera la API: la opción cuyo índice coincide con
 * <code>correcta</code> es la respuesta correcta.
 */
export function construirSolicitud(valor: ValorFormularioPregunta, activa?: boolean): PreguntaRequest {
  return {
    temaId: valor.temaId,
    enunciado: valor.enunciado.trim(),
    dificultad: valor.dificultad,
    tipo: valor.tipo,
    retroalimentacion: valor.retroalimentacion.trim() || null,
    ...(activa === undefined ? {} : { activa }),
    opciones: valor.opciones.map((texto, indice) => ({ texto: texto.trim(), esCorrecta: indice === valor.correcta })),
  };
}
