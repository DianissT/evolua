package com.evolua.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import com.evolua.entity.EstadoIntento;

/**
 * Resultado de un intento: puntuación calculada en el servidor, conteo de respuestas, desempeño por tema,
 * revisión por pregunta y recomendaciones vigentes para los temas del intento.
 */
public record ResultadoIntentoResponse(Long id, Long evaluacionId, String evaluacionNombre, EstadoIntento estado,
        LocalDateTime fechaInicio, LocalDateTime fechaFin, BigDecimal puntuacion, int totalPreguntas, int correctas,
        int incorrectas, int sinResponder, List<RevisionPreguntaResponse> preguntas,
        List<ResultadoTemaResponse> temas, List<RecomendacionResponse> recomendaciones) {
}
