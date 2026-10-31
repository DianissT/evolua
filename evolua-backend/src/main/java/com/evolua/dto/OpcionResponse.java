package com.evolua.dto;

import com.evolua.entity.OpcionPregunta;

/**
 * Opción de respuesta, con la indicación de si es la correcta. Solo se expone a los docentes.
 */
public record OpcionResponse(Long id, String texto, Boolean esCorrecta, Integer orden) {

    public static OpcionResponse desde(OpcionPregunta opcion) {
        return new OpcionResponse(opcion.getId(), opcion.getTexto(), opcion.getEsCorrecta(), opcion.getOrden());
    }
}
