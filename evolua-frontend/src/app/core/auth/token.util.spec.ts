import { tokenVigente } from './token.util';

function crearToken(exp: number): string {
  const codificar = (objeto: object) => btoa(JSON.stringify(objeto)).replace(/=/g, '');
  return `${codificar({ alg: 'HS256' })}.${codificar({ sub: 'ana@uni.edu', exp })}.firma`;
}

describe('tokenVigente', () => {
  it('acepta un token que aún no expira', () => {
    expect(tokenVigente(crearToken(Math.floor(Date.now() / 1000) + 600))).toBe(true);
  });

  it('rechaza un token expirado', () => {
    expect(tokenVigente(crearToken(Math.floor(Date.now() / 1000) - 10))).toBe(false);
  });

  it('rechaza un texto que no es un token', () => {
    expect(tokenVigente('no-es-un-token')).toBe(false);
  });
});
