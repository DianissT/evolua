package com.evolua.service;

import java.util.Collection;
import java.util.Comparator;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.evolua.dto.RecomendacionResponse;
import com.evolua.entity.Recomendacion;
import com.evolua.repository.RecomendacionRepository;

/**
 * Consulta de las recomendaciones vigentes de un estudiante.
 */
@Service
public class RecomendacionService {

    private final RecomendacionRepository recomendaciones;

    public RecomendacionService(RecomendacionRepository recomendaciones) {
        this.recomendaciones = recomendaciones;
    }

    /**
     * Recomendaciones vigentes del estudiante en sus cursos activos, de la más reciente a la más antigua.
     */
    @Transactional(readOnly = true)
    public List<RecomendacionResponse> listarMias(Long estudianteId) {
        return recomendaciones.findByEstudianteIdAndVigenteTrue(estudianteId).stream()
                .filter(recomendacion -> recomendacion.getTema().getCurso().getActivo())
                .sorted(Comparator.comparing(Recomendacion::getFechaCreacion).reversed())
                .map(RecomendacionResponse::desde)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<RecomendacionResponse> vigentesEnTemas(Long estudianteId, Collection<Long> temaIds) {
        return recomendaciones.findByEstudianteIdAndTemaIdInAndVigenteTrue(estudianteId, temaIds).stream()
                .map(RecomendacionResponse::desde)
                .toList();
    }
}
