package com.evolua.dto;

import java.time.LocalDateTime;

import com.evolua.entity.Auditoria;

/**Registro de auditoria para los administradores*/
public record AuditoriaResponse(Long id, Long usuarioId, String usuarioNombre, String accion, String entidad,
        Long entidadId, String detalle, LocalDateTime fecha) {

    public static AuditoriaResponse desde(Auditoria registro) {
        Long usuarioId = registro.getUsuario() != null ? registro.getUsuario().getId() : null;
        String usuarioNombre = registro.getUsuario() != null ? registro.getUsuario().getNombre() : null;
        return new AuditoriaResponse(registro.getId(), usuarioId, usuarioNombre, registro.getAccion(),
                registro.getEntidad(), registro.getEntidadId(), registro.getDetalle(), registro.getFecha());
    }
}
