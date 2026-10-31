package com.evolua.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.evolua.dto.AuditoriaResponse;
import com.evolua.entity.Auditoria;
import com.evolua.repository.AuditoriaRepository;
import com.evolua.repository.UsuarioRepository;

/**
 * Consulta y registro de las operaciones de auditoría realizadas desde la aplicación.
 */
@Service
public class AuditoriaService {

    private final AuditoriaRepository auditoria;
    private final UsuarioRepository usuarios;

    public AuditoriaService(AuditoriaRepository auditoria, UsuarioRepository usuarios) {
        this.auditoria = auditoria;
        this.usuarios = usuarios;
    }

    @Transactional(readOnly = true)
    public List<AuditoriaResponse> listar() {
        return auditoria.findAllByOrderByFechaDesc().stream()
                .map(AuditoriaResponse::desde)
                .toList();
    }

    @Transactional
    public void registrar(Long usuarioId, String accion, String entidad, Long entidadId, String detalle) {
        Auditoria registro = new Auditoria();
        registro.setUsuario(usuarios.getReferenceById(usuarioId));
        registro.setAccion(accion);
        registro.setEntidad(entidad);
        registro.setEntidadId(entidadId);
        registro.setDetalle(detalle);
        auditoria.save(registro);
    }
}
