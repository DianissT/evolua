package com.evolua.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.net.http.HttpResponse;

import org.junit.jupiter.api.Test;

import com.evolua.entity.Rol;

/**
 * Pruebas de las matrículas: permisos del docente y del administrador, duplicados y efecto en los cursos del
 * estudiante.
 */
class MatriculaIntegracionTest extends PruebaApiBase {

    private static final String NUEVO = "nuevo.estudiante@uni.edu";
    private static final String AJENO = "docente.ajeno@uni.edu";

    private static String cuerpo(String correo) {
        return "{\"correo\":\"" + correo + "\"}";
    }

    private static String ruta(Long cursoId) {
        return "/api/v1/courses/" + cursoId + "/enrollments";
    }

    @Test
    void elDocenteMatriculaAUnEstudianteQueLuegoVeElCurso() throws Exception {
        crearUsuario(NUEVO, Rol.ESTUDIANTE);
        Long cursoId = crearCurso("Curso con matrícula");

        HttpResponse<String> respuesta = enviar("POST", ruta(cursoId), LAURA, cuerpo(NUEVO));

        assertEquals(201, respuesta.statusCode());
        assertTrue(respuesta.body().contains("\"estudianteCorreo\":\"" + NUEVO + "\""));
        assertTrue(enviar("GET", "/api/v1/courses", NUEVO, null).body().contains("\"id\":" + cursoId));
    }

    @Test
    void noSePuedeMatricularDosVecesAlMismoEstudiante() throws Exception {
        crearUsuario(NUEVO, Rol.ESTUDIANTE);
        Long cursoId = crearCurso("Curso sin duplicados");
        assertEquals(201, estado("POST", ruta(cursoId), LAURA, cuerpo(NUEVO)));
        assertEquals(409, estado("POST", ruta(cursoId), LAURA, cuerpo(NUEVO)));
    }

    @Test
    void losDatosInvalidosSeRechazan() throws Exception {
        Long cursoId = crearCurso("Curso con datos inválidos");
        assertEquals(404, estado("POST", ruta(cursoId), LAURA, cuerpo("nadie@uni.edu")));
        assertEquals(400, estado("POST", ruta(cursoId), LAURA, cuerpo(LAURA)));
        assertEquals(400, estado("POST", ruta(cursoId), LAURA, cuerpo("no-es-un-correo")));
        assertEquals(400, estado("POST", ruta(cursoId), LAURA, "{}"));
    }

    @Test
    void soloElDocenteResponsableOElAdministradorMatriculan() throws Exception {
        crearUsuario(AJENO, Rol.DOCENTE);
        crearUsuario(NUEVO, Rol.ESTUDIANTE);
        Long cursoId = crearCurso("Curso con permisos");

        assertEquals(403, estado("POST", ruta(cursoId), ANA, cuerpo(NUEVO)));
        assertEquals(403, estado("POST", ruta(cursoId), AJENO, cuerpo(NUEVO)));
        assertEquals(201, estado("POST", ruta(cursoId), ADMIN, cuerpo(NUEVO)));
        assertEquals(200, estado("GET", ruta(cursoId), ADMIN, null));
        assertEquals(403, estado("GET", ruta(cursoId), AJENO, null));
    }

    @Test
    void seListanYSeEliminanLasMatriculas() throws Exception {
        crearUsuario(NUEVO, Rol.ESTUDIANTE);
        Long cursoId = crearCurso("Curso con baja");
        Long estudianteId = jdbc.queryForObject("SELECT id FROM usuarios WHERE correo = ?", Long.class, NUEVO);
        enviar("POST", ruta(cursoId), LAURA, cuerpo(NUEVO));

        assertTrue(enviar("GET", ruta(cursoId), LAURA, null).body().contains("\"estudianteId\":" + estudianteId));
        assertEquals(204, estado("DELETE", ruta(cursoId) + "/" + estudianteId, LAURA, null));
        assertFalse(enviar("GET", ruta(cursoId), LAURA, null).body().contains("\"estudianteId\":" + estudianteId));
        assertEquals(404, estado("DELETE", ruta(cursoId) + "/" + estudianteId, LAURA, null));
        assertFalse(enviar("GET", "/api/v1/courses", NUEVO, null).body().contains("\"id\":" + cursoId));
    }

    @Test
    void noSeMatriculaEnUnCursoInactivo() throws Exception {
        crearUsuario(NUEVO, Rol.ESTUDIANTE);
        Long cursoId = crearCurso("Curso inactivo");
        enviar("DELETE", "/api/v1/courses/" + cursoId, LAURA, null);
        assertEquals(409, estado("POST", ruta(cursoId), LAURA, cuerpo(NUEVO)));
    }
}
