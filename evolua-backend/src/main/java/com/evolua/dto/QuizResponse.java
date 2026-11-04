package com.evolua.dto;

import java.util.List;

import com.evolua.entity.Evaluacion;
import com.evolua.entity.EvaluacionPregunta;
import com.evolua.entity.TipoEvaluacion;

/**
 * Datos de una evaluación fija con sus preguntas. Solo se expone a los docentes.
 */
public record QuizResponse(Long id, Long cursoId, String nombre, TipoEvaluacion tipo, Integer intentosPermitidos,
        Boolean activa, int totalPreguntas, List<QuizPreguntaResponse> preguntas) {

    public static QuizResponse desde(Evaluacion evaluacion, List<EvaluacionPregunta> vinculos) {
        List<QuizPreguntaResponse> preguntas = vinculos.stream().map(QuizPreguntaResponse::desde).toList();
        return new QuizResponse(evaluacion.getId(), evaluacion.getCurso().getId(), evaluacion.getNombre(),
                evaluacion.getTipo(), evaluacion.getIntentosPermitidos(), evaluacion.getActiva(), preguntas.size(),
                preguntas);
    }
}
