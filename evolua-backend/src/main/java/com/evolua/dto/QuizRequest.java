package com.evolua.dto;

import java.util.List;

import com.evolua.entity.TipoEvaluacion;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * Datos para crear o actualizar una evaluación fija. Si no se indica el tipo, es FORMATIVA; si no se indican
 * los intentos permitidos, son ilimitados. El orden de las preguntas es el de la lista.
 */
public record QuizRequest(
        @NotNull(message = "El curso es obligatorio")
        Long cursoId,
        @NotBlank(message = "El nombre es obligatorio")
        @Size(max = 150, message = "El nombre no puede superar los 150 caracteres")
        String nombre,
        TipoEvaluacion tipo,
        @Min(value = 1, message = "Los intentos permitidos deben ser al menos 1")
        Integer intentosPermitidos,
        Boolean activa,
        @NotEmpty(message = "Debe indicarse al menos una pregunta")
        List<@NotNull(message = "Los identificadores de pregunta son obligatorios") Long> preguntaIds) {
}
