package com.evolua.dto;

import jakarta.validation.constraints.NotNull;
/**Cambio de estado que no puede ser nulo */

public record CambioEstadoRequest(@NotNull Boolean activo) {
}
