package com.evolua.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.evolua.dto.AuditoriaResponse;
import com.evolua.repository.AuditoriaRepository;

/**
 * Consulta de los registros de auditoria.
 */
@Service
public class AuditoriaService {

    private final AuditoriaRepository auditoria;

    public AuditoriaService(AuditoriaRepository auditoria) {
        this.auditoria = auditoria;
    }

    @Transactional(readOnly = true)
    public List<AuditoriaResponse> listar() {
        return auditoria.findAllByOrderByFechaDesc().stream()
                .map(AuditoriaResponse::desde)
                .toList();
    }
}
