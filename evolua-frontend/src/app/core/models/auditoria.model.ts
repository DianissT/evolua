export interface RegistroAuditoria {
  id: number;
  usuarioId: number | null;
  accion: string;
  entidad: string;
  entidadId: number | null;
  detalle: string | null;
  fecha: string;
}
