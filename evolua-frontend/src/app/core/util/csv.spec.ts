import { aCsv, celdaCsv } from './csv';

describe('csv', () => {
  it('deja intactos los textos simples y convierte los nulos en celdas vacías', () => {
    expect(celdaCsv('Ana Pérez')).toBe('Ana Pérez');
    expect(celdaCsv(null)).toBe('');
    expect(celdaCsv(undefined)).toBe('');
    expect(celdaCsv(70)).toBe('70');
  });

  it('encierra entre comillas los textos con el separador, comillas o saltos de línea', () => {
    expect(celdaCsv('Gómez, Laura')).toBe('"Gómez, Laura"');
    expect(celdaCsv('dijo "hola"')).toBe('"dijo ""hola"""');
    expect(celdaCsv('línea 1\nlínea 2')).toBe('"línea 1\nlínea 2"');
    expect(celdaCsv('a;b', ';')).toBe('"a;b"');
    expect(celdaCsv('a,b', ';')).toBe('a,b');
  });

  it('neutraliza los textos que Excel interpretaría como fórmula', () => {
    expect(celdaCsv('=SUMA(A1:A9)')).toBe("'=SUMA(A1:A9)");
    expect(celdaCsv('+57 300')).toBe("'+57 300");
    expect(celdaCsv('-1')).toBe("'-1");
    expect(celdaCsv('@usuario')).toBe("'@usuario");
    expect(celdaCsv(-1)).toBe('-1');
  });

  it('une encabezados y filas con el separador indicado', () => {
    const csv = aCsv(['Nombre', 'Promedio'], [['Ana', '70,5'], ['Pedro', null]], ';');
    expect(csv).toBe('Nombre;Promedio\r\nAna;70,5\r\nPedro;');
  });
});
