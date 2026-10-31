package com.evolua.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * Opción de respuesta de una pregunta.
 */
public record OpcionRequest(
        @NotBlank(message = "El texto de la opción es obligatorio")
        @Size(max = 500, message = "El texto de la opción no puede superar los 500 caracteres")
        String texto,
        @NotNull(message = "Debe indicarse si la opción es correcta")
        Boolean esCorrecta) {
}
