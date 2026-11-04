package com.evolua.dto;

import java.time.LocalDateTime;
import java.util.List;

import com.evolua.entity.EstadoIntento;

/**
 * Intento en curso: sus preguntas, sin la respuesta correcta.
 */
public record IntentoResponse(Long id, Long evaluacionId, String evaluacionNombre, EstadoIntento estado,
        LocalDateTime fechaInicio, List<PreguntaIntentoResponse> preguntas) {
}
