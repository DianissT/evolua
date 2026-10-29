package com.evolua.security;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import com.evolua.entity.Rol;

/**
 * Pruebas unitarias de la generación y validación de tokens JWT.
 */
class JwtServiceTest {

    private static final String SECRETO = "clave-de-pruebas-con-mas-de-treinta-y-dos-caracteres-0123456789";
    private static final String OTRO_SECRETO = "otra-clave-de-pruebas-con-mas-de-treinta-y-dos-caracteres-987654";

    private final UsuarioPrincipal ana = new UsuarioPrincipal(3L, "Ana Pérez", "ana.perez@uni.edu", "hash", Rol.ESTUDIANTE, true);

    @Test
    void unTokenValidoDevuelveElCorreoDelUsuario() {
        JwtService servicio = new JwtService(SECRETO, 30);
        String token = servicio.generarToken(ana);
        assertEquals("ana.perez@uni.edu", servicio.extraerCorreo(token).orElseThrow());
    }

    @Test
    void unTokenExpiradoSeRechaza() {
        JwtService servicio = new JwtService(SECRETO, -1);
        assertTrue(servicio.extraerCorreo(servicio.generarToken(ana)).isEmpty());
    }

    @Test
    void unTokenFirmadoConOtraClaveSeRechaza() {
        String token = new JwtService(OTRO_SECRETO, 30).generarToken(ana);
        assertTrue(new JwtService(SECRETO, 30).extraerCorreo(token).isEmpty());
    }

    @Test
    void unTokenAlteradoSeRechaza() {
        JwtService servicio = new JwtService(SECRETO, 30);
        String[] partes = servicio.generarToken(ana).split("\\.");
        String alterado = partes[0] + "." + partes[1] + "x." + partes[2];
        assertTrue(servicio.extraerCorreo(alterado).isEmpty());
    }

    @Test
    void unTextoQueNoEsUnTokenSeRechaza() {
        assertTrue(new JwtService(SECRETO, 30).extraerCorreo("no-es-un-token").isEmpty());
    }

    @Test
    void unSecretoCortoNoEsValido() {
        assertThrows(IllegalStateException.class, () -> new JwtService("corto", 30));
    }
}
