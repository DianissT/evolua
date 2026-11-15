package com.evolua.dto;

import com.evolua.entity.Rol;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * Datos para crear un usuario desde la administración. La contraseña máxima es de 72 caracteres porque BCrypt no
 * usa más de 72 bytes.
 */
public record UsuarioAdminRequest(
        @NotBlank @Size(max = 100) String nombre,
        @NotBlank @Email @Size(max = 150) String correo,
        @NotBlank @Size(min = 8, max = 72) String password,
        @NotNull Rol rol) {
}
