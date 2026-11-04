package com.evolua.dto;

import com.evolua.entity.EvaluacionPregunta;
import com.evolua.entity.TipoPregunta;

/**
 * Pregunta incluida en una evaluación fija, tal como la ve el docente.
 */
public record QuizPreguntaResponse(Long id, String enunciado, Integer dificultad, TipoPregunta tipo, Long temaId,
        Integer orden) {

    public static QuizPreguntaResponse desde(EvaluacionPregunta vinculo) {
        return new QuizPreguntaResponse(vinculo.getPregunta().getId(), vinculo.getPregunta().getEnunciado(),
                vinculo.getPregunta().getDificultad(), vinculo.getPregunta().getTipo(),
                vinculo.getPregunta().getTema().getId(), vinculo.getOrden());
    }
}
