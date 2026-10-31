package com.evolua.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.net.http.HttpResponse;

import org.junit.jupiter.api.Test;

import com.evolua.entity.Rol;

/**
 * Pruebas de la gestión de cursos y temas: permisos por rol, pertenencia, desactivación y auditoría.
 */
class CursoIntegracionTest extends PruebaApiBase {

    @Test
    void elDocenteCreaYConsultaSuCurso() throws Exception {
        Long id = crearCurso("Curso de prueba A");
        HttpResponse<String> consulta = enviar("GET", "/api/v1/courses/" + id, LAURA, null);
        assertEquals(200, consulta.statusCode());
        assertTrue(consulta.body().contains("\"nombre\":\"Curso de prueba A\""));
        assertTrue(enviar("GET", "/api/v1/courses", LAURA, null).body().contains("\"id\":" + id));
    }

    @Test
    void unEstudianteNoPuedeCrearCursos() throws Exception {
        assertEquals(403, estado("POST", "/api/v1/courses", ANA, "{\"nombre\":\"No permitido\"}"));
    }

    @Test
    void elAdministradorNoAccedeALosCursos() throws Exception {
        assertEquals(403, estado("GET", "/api/v1/courses", ADMIN, null));
    }

    @Test
    void sinTokenSeRechazaElAcceso() throws Exception {
        assertEquals(401, estado("GET", "/api/v1/courses", null, null));
    }

    @Test
    void elEstudianteSoloVeLosCursosEnLosQueEstaMatriculado() throws Exception {
        Long seed = jdbc.queryForObject("SELECT id FROM cursos WHERE nombre = 'Fundamentos de Programación'", Long.class);
        assertEquals(200, estado("GET", "/api/v1/courses/" + seed, ANA, null));

        Long nuevo = crearCurso("Curso sin matrícula");
        assertEquals(403, estado("GET", "/api/v1/courses/" + nuevo, ANA, null));
        matricular(ANA, nuevo);
        assertEquals(200, estado("GET", "/api/v1/courses/" + nuevo, ANA, null));
    }

    @Test
    void unDocenteAjenoNoPuedeModificarElCurso() throws Exception {
        crearUsuario("docente.ajeno@uni.edu", Rol.DOCENTE);
        Long id = crearCurso("Curso ajeno");
        String ajeno = "docente.ajeno@uni.edu";
        assertEquals(403, estado("GET", "/api/v1/courses/" + id, ajeno, null));
        assertEquals(403, estado("PUT", "/api/v1/courses/" + id, ajeno, "{\"nombre\":\"Intento\"}"));
        assertEquals(403, estado("DELETE", "/api/v1/courses/" + id, ajeno, null));
    }

    @Test
    void desactivarUnCursoNoLoElimina() throws Exception {
        Long id = crearCurso("Curso a desactivar");
        matricular(ANA, id);
        assertEquals(204, estado("DELETE", "/api/v1/courses/" + id, LAURA, null));

        HttpResponse<String> consulta = enviar("GET", "/api/v1/courses/" + id, LAURA, null);
        assertEquals(200, consulta.statusCode());
        assertTrue(consulta.body().contains("\"activo\":false"));
        int filas = jdbc.queryForObject("SELECT count(*) FROM cursos WHERE id = ?", Integer.class, id);
        assertEquals(1, filas);

        assertEquals(403, estado("GET", "/api/v1/courses/" + id, ANA, null));
        assertFalse(enviar("GET", "/api/v1/courses", ANA, null).body().contains("\"id\":" + id));
    }

    @Test
    void elDocenteActualizaYReactivaSuCurso() throws Exception {
        Long id = crearCurso("Nombre original");
        enviar("DELETE", "/api/v1/courses/" + id, LAURA, null);
        HttpResponse<String> respuesta = enviar("PUT", "/api/v1/courses/" + id, LAURA,
                "{\"nombre\":\"Nombre nuevo\",\"descripcion\":\"Otra\",\"activo\":true}");
        assertEquals(200, respuesta.statusCode());
        assertTrue(respuesta.body().contains("\"nombre\":\"Nombre nuevo\""));
        assertTrue(respuesta.body().contains("\"activo\":true"));
    }

    @Test
    void losDatosInvalidosSeRechazan() throws Exception {
        assertEquals(400, estado("POST", "/api/v1/courses", LAURA, "{\"nombre\":\"\"}"));
        assertEquals(400, estado("POST", "/api/v1/courses", LAURA, "esto no es json"));
        assertEquals(400, estado("GET", "/api/v1/courses/abc", LAURA, null));
        assertEquals(404, estado("GET", "/api/v1/courses/999999999", LAURA, null));
    }

    @Test
    void laCreacionYLaDesactivacionQuedanAuditadas() throws Exception {
        Long id = crearCurso("Curso auditado");
        enviar("DELETE", "/api/v1/courses/" + id, LAURA, null);
        int registros = jdbc.queryForObject(
                "SELECT count(*) FROM auditoria WHERE entidad = 'cursos' AND entidad_id = ? "
                        + "AND usuario_id = (SELECT id FROM usuarios WHERE correo = ?)",
                Integer.class, id, LAURA);
        assertEquals(2, registros);
    }

    @Test
    void elDocenteAdministraLosTemasDeSuCurso() throws Exception {
        Long cursoId = crearCurso("Curso con temas");
        matricular(ANA, cursoId);
        Long temaId = crearTema(cursoId, "Tema uno");

        assertTrue(enviar("GET", "/api/v1/courses/" + cursoId + "/topics", LAURA, null).body().contains("\"id\":" + temaId));
        assertEquals(200, estado("PUT", "/api/v1/topics/" + temaId, LAURA,
                "{\"cursoId\":" + cursoId + ",\"nombre\":\"Tema renombrado\",\"orden\":2}"));
        assertEquals(400, estado("PUT", "/api/v1/topics/" + temaId, LAURA,
                "{\"cursoId\":999999,\"nombre\":\"Cambio de curso\"}"));
        assertEquals(403, estado("POST", "/api/v1/topics", ANA,
                "{\"cursoId\":" + cursoId + ",\"nombre\":\"No permitido\"}"));

        assertEquals(204, estado("DELETE", "/api/v1/topics/" + temaId, LAURA, null));
        assertTrue(enviar("GET", "/api/v1/courses/" + cursoId + "/topics", LAURA, null).body().contains("\"activo\":false"));
        assertFalse(enviar("GET", "/api/v1/courses/" + cursoId + "/topics", ANA, null).body().contains("\"id\":" + temaId));
    }
}
