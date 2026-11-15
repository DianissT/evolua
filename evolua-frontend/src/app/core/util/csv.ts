/**
 * Convierte una celda a texto CSV. Los textos que empiezan por =, +, - o @ se prefijan con una comilla simple para que
 * Excel no los ejecute como fórmula (inyección de fórmulas). Los números llegan ya formateados como texto.
 */
export function celdaCsv(valor: string | number | null | undefined, separador = ','): string {
  if (valor === null || valor === undefined) {
    return '';
  }
  let texto = String(valor);
  if (typeof valor === 'string' && /^[=+\-@\t\r]/.test(texto)) {
    texto = `'${texto}`;
  }
  const necesitaComillas = texto.includes(separador) || /["\r\n]/.test(texto);
  return necesitaComillas ? `"${texto.replace(/"/g, '""')}"` : texto;
}

/** Arma el contenido CSV con una fila de encabezados y las filas de datos. */
export function aCsv(
  encabezados: string[],
  filas: (string | number | null | undefined)[][],
  separador = ',',
): string {
  return [encabezados, ...filas].map((fila) => fila.map((celda) => celdaCsv(celda, separador)).join(separador)).join('\r\n');
}

/** Descarga el contenido como archivo CSV en UTF-8 con BOM, para que Excel respete las tildes. */
export function descargarCsv(nombreArchivo: string, contenido: string): void {
  const blob = new Blob(['\uFEFF', contenido], { type: 'text/csv;charset=utf-8' });
  const url = URL.createObjectURL(blob);
  const enlace = document.createElement('a');
  enlace.href = url;
  enlace.download = nombreArchivo;
  enlace.click();
  URL.revokeObjectURL(url);
}
