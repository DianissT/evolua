export interface PartesEnunciado {
  texto: string;
  codigo: string | null;
}

/**
 * Separa el enunciado de una pregunta en su texto (primera línea) y si lo hay, el código que lo acompaña
 * (las líneas siguientes).
 */
export function partesEnunciado(enunciado: string): PartesEnunciado {
  const lineas = enunciado.split('\n');
  if (lineas.length === 1) {
    return { texto: enunciado, codigo: null };
  }
  return { texto: lineas[0], codigo: lineas.slice(1).join('\n') };
}
