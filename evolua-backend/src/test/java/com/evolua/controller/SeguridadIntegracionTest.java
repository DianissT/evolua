package com.evolua.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.evolua.entity.Rol;
import com.evolua.entity.Usuario;
import com.evolua.repository.UsuarioRepository;

/**
 * Pruebas de autenticación y autorización sobre el servidor en ejecución, con los usuarios de 03_seed.sql.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class SeguridadIntegracionTest {

    private static final String CLAVE = "Evolua2026!";
    private static final Pattern TOKEN = Pattern.compile("\"token\":\"([^\"]+)\"");

    @Value("${local.server.port}")
    private int puerto;

    @Autowired
    private UsuarioRepository usuarios;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private final HttpClient cliente = HttpClient.newHttpClient();

    private HttpResponse<String> enviar(String metodo, String ruta, String token, String cuerpo) throws Exception {
        HttpRequest.Builder solicitud = HttpRequest.newBuilder(URI.create("http://localhost:" + puerto + ruta));
        if (token != null) {
            solicitud.header("Authorization", "Bearer " + token);
        }
        if (cuerpo != null) {
            solicitud.header("Content-Type", "application/json");
            solicitud.method(metodo, HttpRequest.BodyPublishers.ofString(cuerpo));
        } else {
            solicitud.method(metodo, HttpRequest.BodyPublishers.noBody());
        }
        return cliente.send(solicitud.build(), HttpResponse.BodyHandlers.ofString());
    }

    private HttpResponse<String> iniciarSesion(String correo, String clave) throws Exception {
        return enviar("POST", "/api/v1/auth/login", null,
                "{\"correo\":\"" + correo + "\",\"password\":\"" + clave + "\"}");
    }

    private String obtenerToken(String correo) throws Exception {
        Matcher coincidencia = TOKEN.matcher(iniciarSesion(correo, CLAVE).body());
        assertTrue(coincidencia.find());
        return coincidencia.group(1);
    }

    @Test
    void elInicioDeSesionCorrectoDevuelveTokenYUsuario() throws Exception {
        HttpResponse<String> respuesta = iniciarSesion("ana.perez@uni.edu", CLAVE);
        assertEquals(200, respuesta.statusCode());
        assertTrue(TOKEN.matcher(respuesta.body()).find());
        assertTrue(respuesta.body().contains("\"rol\":\"ESTUDIANTE\""));
        assertFalse(respuesta.body().toLowerCase().contains("passwordhash"));
    }

    @Test
    void unaContrasenaIncorrectaDevuelve401() throws Exception {
        assertEquals(401, iniciarSesion("ana.perez@uni.edu", "incorrecta").statusCode());
    }

    @Test
    void unCorreoInexistenteDevuelve401() throws Exception {
        assertEquals(401, iniciarSesion("nadie@uni.edu", CLAVE).statusCode());
    }

    @Test
    void unUsuarioInactivoNoPuedeIniciarSesion() throws Exception {
        Usuario inactivo = new Usuario();
        inactivo.setNombre("Usuario Inactivo");
        inactivo.setCorreo("inactivo@uni.edu");
        inactivo.setPasswordHash(passwordEncoder.encode(CLAVE));
        inactivo.setRol(Rol.ESTUDIANTE);
        inactivo.setActivo(false);
        Usuario guardado = usuarios.save(inactivo);
        try {
            assertEquals(401, iniciarSesion("inactivo@uni.edu", CLAVE).statusCode());
        } finally {
            usuarios.delete(guardado);
        }
    }

    @Test
    void unaSolicitudSinCamposDevuelve400() throws Exception {
        assertEquals(400, enviar("POST", "/api/v1/auth/login", null, "{}").statusCode());
    }

    @Test
    void elPerfilSinTokenDevuelve401() throws Exception {
        assertEquals(401, enviar("GET", "/api/v1/users/me", null, null).statusCode());
    }

    @Test
    void elPerfilConTokenDevuelveElUsuarioAutenticado() throws Exception {
        HttpResponse<String> respuesta = enviar("GET", "/api/v1/users/me", obtenerToken("ana.perez@uni.edu"), null);
        assertEquals(200, respuesta.statusCode());
        assertTrue(respuesta.body().contains("\"correo\":\"ana.perez@uni.edu\""));
    }

    @Test
    void unTokenAlteradoDevuelve401() throws Exception {
        String token = obtenerToken("ana.perez@uni.edu");
        assertEquals(401, enviar("GET", "/api/v1/users/me", token + "x", null).statusCode());
    }

    @Test
    void laAuditoriaSoloEstaDisponibleParaAdministradores() throws Exception {
        assertEquals(403, enviar("GET", "/api/v1/admin/audit", obtenerToken("ana.perez@uni.edu"), null).statusCode());
        assertEquals(403, enviar("GET", "/api/v1/admin/audit", obtenerToken("laura.gomez@uni.edu"), null).statusCode());
        HttpResponse<String> admin = enviar("GET", "/api/v1/admin/audit", obtenerToken("admin@uni.edu"), null);
        assertEquals(200, admin.statusCode());
        assertTrue(admin.body().startsWith("["));
    }
}
