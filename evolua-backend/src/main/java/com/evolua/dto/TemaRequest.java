package com.evolua.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * Datos para crear o actualizar un tema. Si no se indica el orden, el tema se agrega al final del curso.
 */
public record TemaRequest(
        @NotNull(message = "El curso es obligatorio")
        Long cursoId,
        @NotBlank(message = "El nombre es obligatorio")
        @Size(max = 120, message = "El nombre no puede superar los 120 caracteres")
        String nombre,
        @Min(value = 1, message = "El orden debe ser mayor o igual a 1")
        Integer orden,
        Boolean activo) {
}
