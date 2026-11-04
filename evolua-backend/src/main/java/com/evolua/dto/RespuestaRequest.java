package com.evolua.dto;

import jakarta.validation.constraints.NotNull;

/**
 * Opción elegida para una pregunta de un intento.
 */
public record RespuestaRequest(
        @NotNull(message = "La pregunta es obligatoria")
        Long preguntaId,
        @NotNull(message = "La opción es obligatoria")
        Long opcionId) {
}
