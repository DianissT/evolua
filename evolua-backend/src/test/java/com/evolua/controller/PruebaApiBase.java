package com.evolua.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.evolua.entity.Rol;
import com.evolua.entity.Usuario;
import com.evolua.repository.UsuarioRepository;

/**
 * Base de las pruebas de integración de la API. Consume el servidor en ejecución con los usuarios de
 * prueba y elimina al terminar cada prueba los datos que esta haya creado.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
abstract class PruebaApiBase {

    protected static final String CLAVE = "Evolua2026!";
    protected static final String ANA = "ana.perez@uni.edu";
    protected static final String LAURA = "laura.gomez@uni.edu";
    protected static final String ADMIN = "admin@uni.edu";

    private static final Pattern TOKEN = Pattern.compile("\"token\":\"([^\"]+)\"");
    private static final Pattern ID = Pattern.compile("\"id\":(\\d+)");

    @Value("${local.server.port}")
    private int puerto;

    @Autowired
    protected JdbcTemplate jdbc;

    @Autowired
    protected UsuarioRepository usuarios;

    @Autowired
    protected PasswordEncoder passwordEncoder;

    private final HttpClient cliente = HttpClient.newHttpClient();
    private final Map<String, String> tokens = new HashMap<>();
    private final List<Long> cursosCreados = new ArrayList<>();
    private final List<Usuario> usuariosCreados = new ArrayList<>();
    private long ultimaAuditoria;

    @BeforeEach
    void registrarEstadoInicial() {
        ultimaAuditoria = jdbc.queryForObject("SELECT coalesce(max(id), 0) FROM auditoria", Long.class);
    }

    @AfterEach
    void limpiar() {
        for (Long cursoId : cursosCreados) {
            eliminarCurso(cursoId);
        }
        cursosCreados.clear();
        jdbc.update("DELETE FROM auditoria WHERE id > ?", ultimaAuditoria);
        usuarios.deleteAll(usuariosCreados);
        usuariosCreados.clear();
    }

    protected HttpResponse<String> enviar(String metodo, String ruta, String correo, String cuerpo) throws Exception {
        HttpRequest.Builder solicitud = HttpRequest.newBuilder(URI.create("http://localhost:" + puerto + ruta));
        if (correo != null) {
            solicitud.header("Authorization", "Bearer " + token(correo));
        }
        if (cuerpo != null) {
            solicitud.header("Content-Type", "application/json");
            solicitud.method(metodo, HttpRequest.BodyPublishers.ofString(cuerpo));
        } else {
            solicitud.method(metodo, HttpRequest.BodyPublishers.noBody());
        }
        return cliente.send(solicitud.build(), HttpResponse.BodyHandlers.ofString());
    }

    protected int estado(String metodo, String ruta, String correo, String cuerpo) throws Exception {
        return enviar(metodo, ruta, correo, cuerpo).statusCode();
    }

    protected String token(String correo) throws Exception {
        String guardado = tokens.get(correo);
        if (guardado != null) {
            return guardado;
        }
        HttpRequest solicitud = HttpRequest.newBuilder(URI.create("http://localhost:" + puerto + "/api/v1/auth/login"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(
                        "{\"correo\":\"" + correo + "\",\"password\":\"" + CLAVE + "\"}"))
                .build();
        String cuerpo = cliente.send(solicitud, HttpResponse.BodyHandlers.ofString()).body();
        Matcher coincidencia = TOKEN.matcher(cuerpo);
        assertTrue(coincidencia.find());
        tokens.put(correo, coincidencia.group(1));
        return coincidencia.group(1);
    }

    protected Long idDe(HttpResponse<String> respuesta) {
        Matcher coincidencia = ID.matcher(respuesta.body());
        assertTrue(coincidencia.find());
        return Long.valueOf(coincidencia.group(1));
    }

    protected Usuario crearUsuario(String correo, Rol rol) {
        Usuario usuario = new Usuario();
        usuario.setNombre("Usuario de prueba");
        usuario.setCorreo(correo);
        usuario.setPasswordHash(passwordEncoder.encode(CLAVE));
        usuario.setRol(rol);
        Usuario guardado = usuarios.save(usuario);
        usuariosCreados.add(guardado);
        return guardado;
    }

    protected Long crearCurso(String nombre) throws Exception {
        HttpResponse<String> respuesta = enviar("POST", "/api/v1/courses", LAURA,
                "{\"nombre\":\"" + nombre + "\",\"descripcion\":\"Curso de prueba\"}");
        assertEquals(201, respuesta.statusCode());
        Long id = idDe(respuesta);
        cursosCreados.add(id);
        return id;
    }

    protected Long crearTema(Long cursoId, String nombre) throws Exception {
        HttpResponse<String> respuesta = enviar("POST", "/api/v1/topics", LAURA,
                "{\"cursoId\":" + cursoId + ",\"nombre\":\"" + nombre + "\"}");
        assertEquals(201, respuesta.statusCode());
        return idDe(respuesta);
    }

    protected Long crearPregunta(Long temaId, String enunciado) throws Exception {
        return crearPregunta(temaId, enunciado, 1);
    }

    protected Long crearPregunta(Long temaId, String enunciado, int dificultad) throws Exception {
        String cuerpo = "{\"temaId\":" + temaId + ",\"enunciado\":\"" + enunciado + "\",\"dificultad\":" + dificultad + ","
                + "\"tipo\":\"SELECCION_MULTIPLE\",\"retroalimentacion\":\"Explicación de " + enunciado + "\","
                + "\"opciones\":[{\"texto\":\"Correcta\",\"esCorrecta\":true},"
                + "{\"texto\":\"Incorrecta\",\"esCorrecta\":false}]}";
        HttpResponse<String> respuesta = enviar("POST", "/api/v1/questions", LAURA, cuerpo);
        assertEquals(201, respuesta.statusCode());
        return idDe(respuesta);
    }

    protected Long opcionCorrecta(Long preguntaId) {
        return jdbc.queryForObject("SELECT id FROM opciones_pregunta WHERE pregunta_id = ? AND es_correcta", Long.class,
                preguntaId);
    }

    protected Long opcionIncorrecta(Long preguntaId) {
        return jdbc.queryForObject("SELECT id FROM opciones_pregunta WHERE pregunta_id = ? AND NOT es_correcta",
                Long.class, preguntaId);
    }

    protected static String lista(Long... ids) {
        return Arrays.stream(ids).map(String::valueOf).collect(Collectors.joining(","));
    }

    protected Long crearEvaluacion(Long cursoId, String nombre, Integer intentosPermitidos, Long... preguntaIds)
            throws Exception {
        String cuerpo = "{\"cursoId\":" + cursoId + ",\"nombre\":\"" + nombre + "\",\"tipo\":\"FORMATIVA\","
                + "\"intentosPermitidos\":" + intentosPermitidos + ",\"preguntaIds\":[" + lista(preguntaIds) + "]}";
        HttpResponse<String> respuesta = enviar("POST", "/api/v1/quizzes", LAURA, cuerpo);
        assertEquals(201, respuesta.statusCode());
        return idDe(respuesta);
    }

    protected void matricular(String correo, Long cursoId) {
        jdbc.update("INSERT INTO matriculas (estudiante_id, curso_id) SELECT id, ?::bigint FROM usuarios WHERE correo = ?",
                cursoId, correo);
    }

    private void eliminarCurso(Long cursoId) {
        String evaluacionesDelCurso = "SELECT id FROM evaluaciones WHERE curso_id = ?";
        String temasDelCurso = "SELECT id FROM temas WHERE curso_id = ?";
        String preguntasDelCurso = "SELECT p.id FROM preguntas p JOIN temas t ON t.id = p.tema_id WHERE t.curso_id = ?";
        jdbc.execute("ALTER TABLE respuestas DISABLE TRIGGER trg_proteger_respuestas");
        jdbc.execute("ALTER TABLE intentos DISABLE TRIGGER trg_proteger_intento");
        try {
            jdbc.update("DELETE FROM respuestas WHERE intento_id IN (SELECT id FROM intentos WHERE evaluacion_id IN ("
                    + evaluacionesDelCurso + ") OR tema_id IN (" + temasDelCurso + ")) OR pregunta_id IN ("
                    + preguntasDelCurso + ")", cursoId, cursoId, cursoId);
            jdbc.update("DELETE FROM intentos WHERE evaluacion_id IN (" + evaluacionesDelCurso + ") OR tema_id IN ("
                    + temasDelCurso + ")", cursoId, cursoId);
        } finally {
            jdbc.execute("ALTER TABLE intentos ENABLE TRIGGER trg_proteger_intento");
            jdbc.execute("ALTER TABLE respuestas ENABLE TRIGGER trg_proteger_respuestas");
        }
        jdbc.update("DELETE FROM evaluacion_preguntas WHERE evaluacion_id IN (" + evaluacionesDelCurso + ")", cursoId);
        jdbc.update("DELETE FROM evaluaciones WHERE curso_id = ?", cursoId);
        jdbc.update("DELETE FROM opciones_pregunta WHERE pregunta_id IN (" + preguntasDelCurso + ")", cursoId);
        jdbc.update("DELETE FROM preguntas WHERE tema_id IN (" + temasDelCurso + ")", cursoId);
        jdbc.update("DELETE FROM recomendaciones WHERE tema_id IN (" + temasDelCurso + ")", cursoId);
        jdbc.update("DELETE FROM progreso_tema WHERE tema_id IN (" + temasDelCurso + ")", cursoId);
        jdbc.update("DELETE FROM temas WHERE curso_id = ?", cursoId);
        jdbc.update("DELETE FROM matriculas WHERE curso_id = ?", cursoId);
        jdbc.update("DELETE FROM cursos WHERE id = ?", cursoId);
    }
}
