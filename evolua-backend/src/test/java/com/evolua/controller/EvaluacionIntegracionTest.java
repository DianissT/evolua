package com.evolua.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.net.http.HttpResponse;

import org.junit.jupiter.api.Test;

import com.evolua.entity.Rol;

/**
 * Pruebas de las evaluaciones fijas: composición, permisos, edición con intentos y desactivación.
 */
class EvaluacionIntegracionTest extends PruebaApiBase {

    private record Base(Long cursoId, Long temaId, Long primera, Long segunda) {
    }

    private Base base(String nombre) throws Exception {
        Long cursoId = crearCurso(nombre);
        Long temaId = crearTema(cursoId, "Tema de evaluaciones");
        return new Base(cursoId, temaId, crearPregunta(temaId, "Pregunta uno"), crearPregunta(temaId, "Pregunta dos"));
    }

    private static String cuerpo(Long cursoId, String nombre, String intentos, Long... preguntaIds) {
        return "{\"cursoId\":" + cursoId + ",\"nombre\":\"" + nombre + "\",\"tipo\":\"FORMATIVA\","
                + "\"intentosPermitidos\":" + intentos + ",\"preguntaIds\":[" + lista(preguntaIds) + "]}";
    }

    @Test
    void elDocenteCreaUnaEvaluacionConPreguntasDeSuCurso() throws Exception {
        Base b = base("Curso de evaluaciones");
        HttpResponse<String> creada = enviar("POST", "/api/v1/quizzes", LAURA,
                cuerpo(b.cursoId(), "Evaluación inicial", "null", b.primera(), b.segunda()));
        assertEquals(201, creada.statusCode());
        assertTrue(creada.body().contains("\"totalPreguntas\":2"));
        Long id = idDe(creada);

        HttpResponse<String> consulta = enviar("GET", "/api/v1/quizzes/" + id, LAURA, null);
        assertEquals(200, consulta.statusCode());
        assertTrue(consulta.body().contains("\"id\":" + b.primera()));
        assertTrue(consulta.body().contains("\"id\":" + b.segunda()));
        int vinculos = jdbc.queryForObject("SELECT count(*) FROM evaluacion_preguntas WHERE evaluacion_id = ?",
                Integer.class, id);
        assertEquals(2, vinculos);
    }

    @Test
    void elEstudianteVeLasEvaluacionesSoloSiEstaMatriculado() throws Exception {
        Base b = base("Curso con listado");
        Long id = crearEvaluacion(b.cursoId(), "Evaluación visible", 2, b.primera(), b.segunda());

        assertEquals(403, estado("GET", "/api/v1/courses/" + b.cursoId() + "/quizzes", ANA, null));
        assertTrue(enviar("GET", "/api/v1/courses/" + b.cursoId() + "/quizzes", LAURA, null).body().contains("\"id\":" + id));

        matricular(ANA, b.cursoId());
        HttpResponse<String> lista = enviar("GET", "/api/v1/courses/" + b.cursoId() + "/quizzes", ANA, null);
        assertEquals(200, lista.statusCode());
        assertTrue(lista.body().contains("\"intentosUsados\":0"));
        assertTrue(lista.body().contains("\"intentosPermitidos\":2"));
    }

    @Test
    void laComposicionInvalidaSeRechaza() throws Exception {
        Base b = base("Curso de composición");
        Base otro = base("Otro curso");
        assertEquals(400, estado("POST", "/api/v1/quizzes", LAURA, cuerpo(b.cursoId(), "Ajena", "null", otro.primera())));
        assertEquals(400, estado("POST", "/api/v1/quizzes", LAURA,
                cuerpo(b.cursoId(), "Repetida", "null", b.primera(), b.primera())));
        assertEquals(404, estado("POST", "/api/v1/quizzes", LAURA, cuerpo(b.cursoId(), "Inexistente", "null", 999999999L)));
        assertEquals(400, estado("POST", "/api/v1/quizzes", LAURA, cuerpo(b.cursoId(), "Vacía", "null")));
        enviar("DELETE", "/api/v1/questions/" + b.segunda(), LAURA, null);
        assertEquals(400, estado("POST", "/api/v1/quizzes", LAURA, cuerpo(b.cursoId(), "Inactiva", "null", b.segunda())));
    }

    @Test
    void losDatosInvalidosSeRechazan() throws Exception {
        Base b = base("Curso de datos");
        assertEquals(400, estado("POST", "/api/v1/quizzes", LAURA, cuerpo(b.cursoId(), "", "null", b.primera())));
        assertEquals(400, estado("POST", "/api/v1/quizzes", LAURA, cuerpo(b.cursoId(), "Sin intentos", "0", b.primera())));
    }

    @Test
    void soloElDocenteResponsableCreaEvaluaciones() throws Exception {
        crearUsuario("docente.ajeno@uni.edu", Rol.DOCENTE);
        Base b = base("Curso de permisos");
        String cuerpo = cuerpo(b.cursoId(), "No permitida", "null", b.primera());
        assertEquals(403, estado("POST", "/api/v1/quizzes", ANA, cuerpo));
        assertEquals(403, estado("POST", "/api/v1/quizzes", "docente.ajeno@uni.edu", cuerpo));
    }

    @Test
    void sinIntentosSePuedenCambiarLasPreguntas() throws Exception {
        Base b = base("Curso editable");
        Long id = crearEvaluacion(b.cursoId(), "Editable", null, b.primera(), b.segunda());

        HttpResponse<String> respuesta = enviar("PUT", "/api/v1/quizzes/" + id, LAURA,
                cuerpo(b.cursoId(), "Editable", "null", b.primera()));

        assertEquals(200, respuesta.statusCode());
        assertTrue(respuesta.body().contains("\"totalPreguntas\":1"));
        int vinculos = jdbc.queryForObject("SELECT count(*) FROM evaluacion_preguntas WHERE evaluacion_id = ?",
                Integer.class, id);
        assertEquals(1, vinculos);
    }

    @Test
    void conIntentosSoloSePuedeRenombrar() throws Exception {
        Base b = base("Curso con intentos");
        Long id = crearEvaluacion(b.cursoId(), "Original", null, b.primera(), b.segunda());
        matricular(ANA, b.cursoId());
        assertEquals(201, estado("POST", "/api/v1/quizzes/" + id + "/attempts", ANA, null));

        assertEquals(200, estado("PUT", "/api/v1/quizzes/" + id, LAURA,
                cuerpo(b.cursoId(), "Renombrada", "null", b.primera(), b.segunda())));
        assertEquals(409, estado("PUT", "/api/v1/quizzes/" + id, LAURA,
                cuerpo(b.cursoId(), "Renombrada", "null", b.primera())));
    }

    @Test
    void desactivarUnaEvaluacionLaOcultaAlEstudiante() throws Exception {
        Base b = base("Curso con baja");
        Long id = crearEvaluacion(b.cursoId(), "A desactivar", null, b.primera());
        matricular(ANA, b.cursoId());

        assertEquals(204, estado("DELETE", "/api/v1/quizzes/" + id, LAURA, null));

        assertFalse(enviar("GET", "/api/v1/courses/" + b.cursoId() + "/quizzes", ANA, null).body().contains("\"id\":" + id));
        assertEquals(409, estado("POST", "/api/v1/quizzes/" + id + "/attempts", ANA, null));
        int filas = jdbc.queryForObject("SELECT count(*) FROM evaluaciones WHERE id = ?", Integer.class, id);
        assertEquals(1, filas);
    }
}
