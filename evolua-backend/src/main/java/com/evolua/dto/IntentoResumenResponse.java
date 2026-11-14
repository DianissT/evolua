package com.evolua.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.evolua.entity.EstadoIntento;
import com.evolua.entity.Intento;
import com.evolua.entity.TipoIntento;

/**
 * Resumen de un intento para el historial del estudiante.
 */
public record IntentoResumenResponse(Long id, TipoIntento tipo, Long evaluacionId, String evaluacionNombre,
        Long temaId, String temaNombre, EstadoIntento estado, LocalDateTime fechaInicio, LocalDateTime fechaFin,
        BigDecimal puntuacion) {
    public static IntentoResumenResponse desde(Intento intento) {
        Long evaluacionId = intento.getEvaluacion() != null ? intento.getEvaluacion().getId() : null;
        String evaluacionNombre = intento.getEvaluacion() != null ? intento.getEvaluacion().getNombre() : null;
        Long temaId = intento.getTema() != null ? intento.getTema().getId() : null;
        String temaNombre = intento.getTema() != null ? intento.getTema().getNombre() : null;
        return new IntentoResumenResponse(intento.getId(), intento.getTipo(), evaluacionId, evaluacionNombre, temaId,
                temaNombre, intento.getEstado(), intento.getFechaInicio(), intento.getFechaFin(),
                intento.getPuntuacion());
    }
}
