/**
 * Indica si un token JWT aún no ha expirado, según su campo exp. No valida la firma: eso lo hace el servidor.
 */
export function tokenVigente(token: string): boolean {
  try {
    const carga = token.split('.')[1].replace(/-/g, '+').replace(/_/g, '/');
    const datos = JSON.parse(atob(carga)) as { exp?: number };
    return typeof datos.exp === 'number' && datos.exp * 1000 > Date.now();
  } catch {
    return false;
  }
}
