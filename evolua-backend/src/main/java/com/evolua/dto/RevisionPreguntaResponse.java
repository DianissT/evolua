package com.evolua.dto;

import java.util.List;

import com.evolua.entity.TipoPregunta;

/**
 * Pregunta de un intento finalizado: respuesta del estudiante, respuesta correcta y retroalimentación.
 */
public record RevisionPreguntaResponse(Long id, String enunciado, TipoPregunta tipo,
        List<OpcionRevisionResponse> opciones, Long opcionSeleccionadaId, boolean esCorrecta,
        String retroalimentacion) {
}
