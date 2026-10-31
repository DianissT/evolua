package com.evolua.dto;

import java.util.List;

import com.evolua.entity.OpcionPregunta;
import com.evolua.entity.Pregunta;
import com.evolua.entity.TipoPregunta;

/**
 * Datos de una pregunta con sus opciones. Solo se expone a los docentes porque incluye la respuesta correcta.
 */
public record PreguntaResponse(Long id, Long temaId, String enunciado, Integer dificultad, TipoPregunta tipo,
        String retroalimentacion, Boolean activa, List<OpcionResponse> opciones) {

    public static PreguntaResponse desde(Pregunta pregunta, List<OpcionPregunta> opciones) {
        return new PreguntaResponse(pregunta.getId(), pregunta.getTema().getId(), pregunta.getEnunciado(),
                pregunta.getDificultad(), pregunta.getTipo(), pregunta.getRetroalimentacion(), pregunta.getActiva(),
                opciones.stream().map(OpcionResponse::desde).toList());
    }
}
