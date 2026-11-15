package com.evolua.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.net.http.HttpResponse;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import com.evolua.entity.Rol;
import com.evolua.entity.Usuario;

/**
 * Pruebas de la gestión de usuarios y roles.
 */
class AdminUsuarioIntegracionTest extends PruebaApiBase {

    private static final String NUEVO = "nuevo.usuario@test.edu";
    private static final String BASE = "/api/v1/admin/users";

    @AfterEach
    void borrarUsuarioCreadoPorLaApi() {
        usuarios.findByCorreo(NUEVO).ifPresent(usuarios::delete);
    }

    private Long idAdmin() {
        return jdbc.queryForObject("SELECT id FROM usuarios WHERE correo = ?", Long.class, ADMIN);
    }

    private String cuerpoUsuario(String correo, String password, String rol) {
        return "{\"nombre\":\"Usuario nuevo\",\"correo\":\"" + correo + "\",\"password\":\"" + password
                + "\",\"rol\":\"" + rol + "\"}";
    }

    private int intentarLogin(String correo) throws Exception {
        return estado("POST", "/api/v1/auth/login", null,
                "{\"correo\":\"" + correo + "\",\"password\":\"" + CLAVE + "\"}");
    }

    private int auditorias(String accion, Long entidadId) {
        return jdbc.queryForObject("SELECT count(*) FROM auditoria WHERE accion = ? AND entidad = 'usuarios' "
                + "AND entidad_id = ?", Integer.class, accion, entidadId);
    }

    @Test
    void soloElAdministradorListaUsuariosYNuncaVeContrasenas() throws Exception {
        assertEquals(401, estado("GET", BASE, null, null));
        assertEquals(403, estado("GET", BASE, ANA, null));
        assertEquals(403, estado("GET", BASE, LAURA, null));

        HttpResponse<String> respuesta = enviar("GET", BASE, ADMIN, null);
        assertEquals(200, respuesta.statusCode());
        assertTrue(respuesta.body().contains(LAURA));
        assertFalse(respuesta.body().toLowerCase().contains("password"));
    }

    @Test
    void elAdministradorCreaUnUsuarioConContrasenaBcrypt() throws Exception {
        assertEquals(403, estado("POST", BASE, LAURA, cuerpoUsuario(NUEVO, CLAVE, "ESTUDIANTE")));

        HttpResponse<String> respuesta = enviar("POST", BASE, ADMIN, cuerpoUsuario(NUEVO, CLAVE, "ESTUDIANTE"));
        assertEquals(201, respuesta.statusCode());
        assertTrue(respuesta.body().contains("\"rol\":\"ESTUDIANTE\""));
        assertFalse(respuesta.body().toLowerCase().contains("password"));

        String hash = jdbc.queryForObject("SELECT password_hash FROM usuarios WHERE correo = ?", String.class, NUEVO);
        assertTrue(hash.startsWith("$2"));
        assertNotEquals(CLAVE, hash);
        assertEquals(200, intentarLogin(NUEVO));

        Long id = idDe(respuesta);
        assertEquals(1, auditorias("CREAR", id));
        String detalle = jdbc.queryForObject("SELECT detalle FROM auditoria WHERE accion = 'CREAR' "
                + "AND entidad = 'usuarios' AND entidad_id = ?", String.class, id);
        assertFalse(detalle.contains(CLAVE));
    }

    @Test
    void noSePermiteUnCorreoRepetido() throws Exception {
        assertEquals(409, estado("POST", BASE, ADMIN, cuerpoUsuario(LAURA, CLAVE, "DOCENTE")));
        assertEquals(201, estado("POST", BASE, ADMIN, cuerpoUsuario(NUEVO, CLAVE, "ESTUDIANTE")));
        assertEquals(409, estado("POST", BASE, ADMIN, cuerpoUsuario(NUEVO.toUpperCase(), CLAVE, "ESTUDIANTE")));
    }

    @Test
    void laSolicitudInvalidaDevuelve400() throws Exception {
        assertEquals(400, estado("POST", BASE, ADMIN, cuerpoUsuario(NUEVO, "corta", "ESTUDIANTE")));
        assertEquals(400, estado("POST", BASE, ADMIN, cuerpoUsuario("no-es-correo", CLAVE, "ESTUDIANTE")));
        assertEquals(400, estado("POST", BASE, ADMIN, cuerpoUsuario(NUEVO, CLAVE, "SUPERUSUARIO")));
        assertEquals(400, estado("POST", BASE, ADMIN, cuerpoUsuario(NUEVO, "x".repeat(73), "ESTUDIANTE")));
        assertEquals(400, estado("POST", BASE, ADMIN, "{}"));
    }

    @Test
    void elCambioDeRolLoRegistraElTriggerConElAdministrador() throws Exception {
        Usuario usuario = crearUsuario("rol.cambio@test.edu", Rol.ESTUDIANTE);

        assertEquals(403, estado("PUT", BASE + "/" + usuario.getId() + "/role", LAURA, "{\"rol\":\"DOCENTE\"}"));
        HttpResponse<String> respuesta = enviar("PUT", BASE + "/" + usuario.getId() + "/role", ADMIN,
                "{\"rol\":\"DOCENTE\"}");

        assertEquals(200, respuesta.statusCode());
        assertTrue(respuesta.body().contains("\"rol\":\"DOCENTE\""));
        assertEquals("DOCENTE", jdbc.queryForObject("SELECT rol FROM usuarios WHERE id = ?", String.class,
                usuario.getId()));
        int registros = jdbc.queryForObject("SELECT count(*) FROM auditoria WHERE accion = 'CAMBIO_ROL' "
                + "AND entidad_id = ? AND usuario_id = ?", Integer.class, usuario.getId(), idAdmin());
        assertEquals(1, registros);
    }

    @Test
    void protegeLaCuentaDelAdministradorYLosDocentesConCursos() throws Exception {
        assertEquals(400, estado("PUT", BASE + "/" + idAdmin() + "/role", ADMIN, "{\"rol\":\"DOCENTE\"}"));
        Long laura = jdbc.queryForObject("SELECT id FROM usuarios WHERE correo = ?", Long.class, LAURA);
        assertEquals(409, estado("PUT", BASE + "/" + laura + "/role", ADMIN, "{\"rol\":\"ESTUDIANTE\"}"));
        assertEquals("DOCENTE", jdbc.queryForObject("SELECT rol FROM usuarios WHERE id = ?", String.class, laura));
        assertEquals(404, estado("PUT", BASE + "/999999999/role", ADMIN, "{\"rol\":\"DOCENTE\"}"));
    }

    @Test
    void unUsuarioDesactivadoNoPuedeIniciarSesion() throws Exception {
        Usuario usuario = crearUsuario("inactivar@test.edu", Rol.ESTUDIANTE);
        assertEquals(200, intentarLogin(usuario.getCorreo()));

        assertEquals(200, estado("PUT", BASE + "/" + usuario.getId() + "/active", ADMIN, "{\"activo\":false}"));
        assertEquals(401, intentarLogin(usuario.getCorreo()));
        assertEquals(1, auditorias("DESACTIVAR", usuario.getId()));

        assertEquals(200, estado("PUT", BASE + "/" + usuario.getId() + "/active", ADMIN, "{\"activo\":true}"));
        assertEquals(200, intentarLogin(usuario.getCorreo()));
        assertEquals(1, auditorias("ACTIVAR", usuario.getId()));
    }

    @Test
    void elAdministradorNoPuedeDesactivarseASiMismo() throws Exception {
        assertEquals(400, estado("PUT", BASE + "/" + idAdmin() + "/active", ADMIN, "{\"activo\":false}"));
        assertEquals(403, estado("PUT", BASE + "/" + idAdmin() + "/active", ANA, "{\"activo\":false}"));
    }
}
