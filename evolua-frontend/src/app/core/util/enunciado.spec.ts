import { partesEnunciado } from './enunciado';

describe('partesEnunciado', () => {
  it('un enunciado de una línea no tiene código', () => {
    expect(partesEnunciado('¿Qué es una variable?')).toEqual({ texto: '¿Qué es una variable?', codigo: null });
  });

  it('separa la primera línea del código que la acompaña', () => {
    const partes = partesEnunciado('¿Qué imprime este código?\nint x = 5;\nSystem.out.println(x);');
    expect(partes.texto).toBe('¿Qué imprime este código?');
    expect(partes.codigo).toBe('int x = 5;\nSystem.out.println(x);');
  });
});
