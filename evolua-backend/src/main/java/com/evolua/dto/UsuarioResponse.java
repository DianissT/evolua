package com.evolua.dto;

import com.evolua.entity.Rol;
import com.evolua.entity.Usuario;
import com.evolua.security.UsuarioPrincipal;

/**
 * Datos publicos de un usuario sin la contraseña.
 */
public record UsuarioResponse(Long id, String nombre, String correo, Rol rol) {

    public static UsuarioResponse desde(Usuario usuario) {
        return new UsuarioResponse(usuario.getId(), usuario.getNombre(), usuario.getCorreo(), usuario.getRol());
    }

    public static UsuarioResponse desde(UsuarioPrincipal usuario) {
        return new UsuarioResponse(usuario.getId(), usuario.getNombre(), usuario.getUsername(), usuario.getRol());
    }
}
