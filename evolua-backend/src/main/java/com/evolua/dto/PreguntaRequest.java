package com.evolua.dto;

import java.util.List;

import com.evolua.entity.TipoPregunta;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

/**
 * Datos para crear o actualizar una pregunta con sus opciones. El campo activa solo se considera al actualizar.
 */
public record PreguntaRequest(
        @NotNull(message = "El tema es obligatorio")
        Long temaId,
        @NotBlank(message = "El enunciado es obligatorio")
        String enunciado,
        @NotNull(message = "La dificultad es obligatoria")
        @Min(value = 1, message = "La dificultad debe estar entre 1 y 3")
        @Max(value = 3, message = "La dificultad debe estar entre 1 y 3")
        Integer dificultad,
        @NotNull(message = "El tipo de pregunta es obligatorio")
        TipoPregunta tipo,
        String retroalimentacion,
        Boolean activa,
        @NotEmpty(message = "Debe indicarse al menos una opción")
        @Valid
        List<OpcionRequest> opciones) {
}
