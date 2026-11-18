package com.evolua.dto;

import com.evolua.entity.Rol;

import jakarta.validation.constraints.NotNull;
/**Cambio de rol que no puede ser nulo */

public record CambioRolRequest(@NotNull Rol rol) {
}
