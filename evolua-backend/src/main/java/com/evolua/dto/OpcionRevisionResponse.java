package com.evolua.dto;

/**
 * Opción de respuesta con la indicación de si es la correcta. Solo se expone al finalizar el intento.
 */
public record OpcionRevisionResponse(Long id, String texto, boolean esCorrecta) {
}
