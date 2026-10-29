package com.evolua.dto;

/**
 * Inicio de sesión correcto respo de con token de acceso y datos del usuario
 */
public record LoginResponse(String token, String tipo, long expiraEnSegundos, UsuarioResponse usuario) {
}
