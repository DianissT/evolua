import { construirSolicitud, opcionesIniciales } from './question-form.util';

describe('question-form.util', () => {
  it('una pregunta de selección múltiple comienza con cuatro opciones vacías', () => {
    expect(opcionesIniciales('SELECCION_MULTIPLE')).toEqual(['', '', '', '']);
  });

  it('una pregunta de verdadero o falso comienza con sus dos opciones', () => {
    expect(opcionesIniciales('VERDADERO_FALSO')).toEqual(['Verdadero', 'Falso']);
  });

  it('marca como correcta solo la opción elegida', () => {
    const solicitud = construirSolicitud({
      temaId: 7,
      dificultad: 2,
      tipo: 'SELECCION_MULTIPLE',
      enunciado: '  ¿Cuánto es 2 + 2?  ',
      retroalimentacion: '   ',
      correcta: 1,
      opciones: ['3', ' 4 ', '5'],
    });

    expect(solicitud.enunciado).toBe('¿Cuánto es 2 + 2?');
    expect(solicitud.retroalimentacion).toBeNull();
    expect(solicitud.opciones).toEqual([
      { texto: '3', esCorrecta: false },
      { texto: '4', esCorrecta: true },
      { texto: '5', esCorrecta: false },
    ]);
    expect('activa' in solicitud).toBe(false);
  });

  it('incluye activa solo cuando se indica', () => {
    const solicitud = construirSolicitud(
      { temaId: 1, dificultad: 1, tipo: 'VERDADERO_FALSO', enunciado: 'x', retroalimentacion: '', correcta: 0, opciones: ['Verdadero', 'Falso'] },
      true,
    );
    expect(solicitud.activa).toBe(true);
  });
});
