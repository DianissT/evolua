package com.evolua.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Datos para crear o actualizar un curso. El campo activo solo se considera al actualizar.
 */
public record CursoRequest(
        @NotBlank(message = "El nombre es obligatorio")
        @Size(max = 150, message = "El nombre no puede superar los 150 caracteres")
        String nombre,
        String descripcion,
        Boolean activo) {
}
