import { claseNivel, colorActividad, colorNivel } from './nivel';

describe('nivel', () => {
  it('asigna un color a cada nivel de dominio', () => {
    expect(colorNivel('REFUERZO')).toBe('warning');
    expect(colorNivel('INTERMEDIO')).toBe('primary');
    expect(colorNivel('AVANZADO')).toBe('success');
    expect(colorNivel('DOMINADO')).toBe('success');
    expect(colorNivel('SIN_DATOS')).toBe('secondary');
    expect(claseNivel('REFUERZO')).toBe('text-bg-warning');
  });

  it('asigna un color a cada actividad recomendada', () => {
    expect(colorActividad('REFUERZO')).toBe('warning');
    expect(colorActividad('PRACTICA_ADICIONAL')).toBe('primary');
    expect(colorActividad('CONFIRMACION')).toBe('success');
    expect(colorActividad('AVANZADO')).toBe('success');
    expect(colorActividad(undefined)).toBe('secondary');
  });
});
