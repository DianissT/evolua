/** Formatea un número según el idioma de la interfaz.*/
export function formatearNumero(valor: number, idioma: string, maximoDecimales = 2): string {
  return new Intl.NumberFormat(idioma, { maximumFractionDigits: maximoDecimales }).format(valor);
}
