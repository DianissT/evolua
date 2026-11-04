package com.evolua.dto;

import com.evolua.entity.Evaluacion;
import com.evolua.entity.TipoEvaluacion;

/**
 * Resumen de una evaluación fija. Para los estudiantes incluye los intentos que ya usaron.
 */
public record QuizResumenResponse(Long id, String nombre, TipoEvaluacion tipo, Integer intentosPermitidos,
        Boolean activa, long totalPreguntas, Long intentosUsados) {

    public static QuizResumenResponse desde(Evaluacion evaluacion, long totalPreguntas, Long intentosUsados) {
        return new QuizResumenResponse(evaluacion.getId(), evaluacion.getNombre(), evaluacion.getTipo(),
                evaluacion.getIntentosPermitidos(), evaluacion.getActiva(), totalPreguntas, intentosUsados);
    }
}
