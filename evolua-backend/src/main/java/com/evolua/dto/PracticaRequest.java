package com.evolua.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

/**
 * Solicitud para iniciar una práctica por tema. En la modalidad AUTOMATICA no se envían dificultad ni cantidad;
 * en la modalidad LIBRE ambas son opcionales (sin dificultad se admiten las tres; sin cantidad se usa la
 * configurada en el sistema).
 */
public record PracticaRequest(
        @NotNull(message = "La modalidad es obligatoria")
        ModalidadPractica modalidad,
        @Min(value = 1, message = "La dificultad debe estar entre 1 y 3")
        @Max(value = 3, message = "La dificultad debe estar entre 1 y 3")
        Integer dificultad,
        @Min(value = 1, message = "La cantidad debe estar entre 1 y 20")
        @Max(value = 20, message = "La cantidad debe estar entre 1 y 20")
        Integer cantidad) {
}
