package com.evolua.dto;

import com.evolua.entity.Rol;

import jakarta.validation.constraints.NotNull;

public record CambioRolRequest(@NotNull Rol rol) {
}
