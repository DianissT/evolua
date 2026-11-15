package com.evolua.dto;

import jakarta.validation.constraints.NotNull;

public record CambioEstadoRequest(@NotNull Boolean activo) {
}
