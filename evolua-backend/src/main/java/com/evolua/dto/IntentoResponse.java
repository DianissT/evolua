package com.evolua.dto;

import java.time.LocalDateTime;
import java.util.List;

import com.evolua.entity.EstadoIntento;
import com.evolua.entity.TipoIntento;

/**
 * Intento en curso: sus preguntas, sin la respuesta correcta.
 */
public record IntentoResponse(Long id, TipoIntento tipo, Long evaluacionId, String evaluacionNombre, Long temaId,
        String temaNombre, EstadoIntento estado, LocalDateTime fechaInicio, List<PreguntaIntentoResponse> preguntas) {
}
