package com.evolua.dto;

import java.time.LocalDateTime;

import com.evolua.entity.Rol;
import com.evolua.entity.Usuario;

/**
 * Usuario como lo ve el administrador, sin contraseña ni hash.
 */
public record UsuarioAdminResponse(Long id, String nombre, String correo, Rol rol, boolean activo,
        LocalDateTime fechaCreacion) {

    public static UsuarioAdminResponse desde(Usuario usuario) {
        return new UsuarioAdminResponse(usuario.getId(), usuario.getNombre(), usuario.getCorreo(), usuario.getRol(),
                Boolean.TRUE.equals(usuario.getActivo()), usuario.getFechaCreacion());
    }
}
