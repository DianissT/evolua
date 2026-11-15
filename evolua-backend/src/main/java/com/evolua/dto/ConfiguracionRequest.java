package com.evolua.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** Nuevo valor de un parámetro. Todos los parámetros actuales son números enteros. */
public record ConfiguracionRequest(@NotBlank @Size(max = 200) String valor) {
}
