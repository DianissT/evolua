import { NivelDominio, TipoActividad } from '../models/progreso.model';

/**Color de Bootstrap asociado a un nivel de dominio.*/
export function colorNivel(nivel: NivelDominio): string {
  switch (nivel) {
    case 'REFUERZO':
      return 'warning';
    case 'INTERMEDIO':
      return 'primary';
    case 'AVANZADO':
    case 'DOMINADO':
      return 'success';
    default:
      return 'secondary';
  }
}

/**Clase de Bootstrap para la etiqueta de un nivel de dominio.*/
export function claseNivel(nivel: NivelDominio): string {
  return `text-bg-${colorNivel(nivel)}`;
}

/**Color de Bootstrap asociado a una actividad recomendada.*/
export function colorActividad(actividad: TipoActividad | undefined): string {
  switch (actividad) {
    case 'REFUERZO':
      return 'warning';
    case 'PRACTICA_ADICIONAL':
      return 'primary';
    case 'CONFIRMACION':
    case 'AVANZADO':
      return 'success';
    default:
      return 'secondary';
  }
}
