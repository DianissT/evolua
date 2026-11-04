package com.evolua.dto;

import java.util.List;

import com.evolua.entity.TipoPregunta;

/**
 * Pregunta de un intento en curso, con la opción que el estudiante ha seleccionado hasta el momento.
 */
public record PreguntaIntentoResponse(Long id, String enunciado, TipoPregunta tipo,
        List<OpcionIntentoResponse> opciones, Long opcionSeleccionadaId) {
}
