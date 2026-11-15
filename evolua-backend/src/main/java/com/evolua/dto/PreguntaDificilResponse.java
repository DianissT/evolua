package com.evolua.dto;

import java.math.BigDecimal;

/**
 * Pregunta pocos aciertos entre los estudiantes del curso.
 */
public record PreguntaDificilResponse(Long preguntaId, String enunciado, Long temaId, String temaNombre,
        int dificultad, long totalRespuestas, long respuestasCorrectas, BigDecimal porcentajeAciertos) {
}
