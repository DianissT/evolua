package com.evolua.dto;

import java.time.LocalDateTime;

import com.evolua.entity.Auditoria;

/**
 * Registro de auditoria para los administradores
 */
public record AuditoriaResponse(Long id, Long usuarioId, String accion, String entidad, Long entidadId,
        String detalle, LocalDateTime fecha) {

    public static AuditoriaResponse desde(Auditoria registro) {
        Long usuarioId = registro.getUsuario() != null ? registro.getUsuario().getId() : null;
        return new AuditoriaResponse(registro.getId(), usuarioId, registro.getAccion(), registro.getEntidad(),
                registro.getEntidadId(), registro.getDetalle(), registro.getFecha());
    }
}
