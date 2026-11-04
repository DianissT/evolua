package com.evolua.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.net.http.HttpResponse;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.dao.DataAccessException;

/**
 * Pruebas de los intentos: inicio, respuestas, calificación en el servidor, finalización y acceso a los resultados.
 */
class IntentoIntegracionTest extends PruebaApiBase {

    private static final String PEDRO = "pedro.soto@uni.edu";

    private record Escenario(Long cursoId, List<Long> preguntas, Long evaluacionId) {
    }

    private Escenario escenario(Integer intentosPermitidos) throws Exception {
        Long cursoId = crearCurso("Curso de intentos");
        Long temaId = crearTema(cursoId, "Tema de intentos");
        List<Long> preguntas = List.of(crearPregunta(temaId, "P1"), crearPregunta(temaId, "P2"),
                crearPregunta(temaId, "P3"));
        Long evaluacionId = crearEvaluacion(cursoId, "Evaluación de intentos", intentosPermitidos,
                preguntas.toArray(new Long[0]));
        matricular(ANA, cursoId);
        return new Escenario(cursoId, preguntas, evaluacionId);
    }

    private static String rutaIntentos(Long evaluacionId) {
        return "/api/v1/quizzes/" + evaluacionId + "/attempts";
    }

    private Long iniciar(Escenario escenario) throws Exception {
        HttpResponse<String> respuesta = enviar("POST", rutaIntentos(escenario.evaluacionId()), ANA, null);
        assertEquals(201, respuesta.statusCode());
        return idDe(respuesta);
    }

    private int responder(Long intentoId, Long preguntaId, Long opcionId, String correo) throws Exception {
        return estado("POST", "/api/v1/attempts/" + intentoId + "/answers", correo,
                "{\"preguntaId\":" + preguntaId + ",\"opcionId\":" + opcionId + "}");
    }

    private HttpResponse<String> finalizar(Long intentoId, String cuerpo) throws Exception {
        return enviar("POST", "/api/v1/attempts/" + intentoId + "/submit", ANA, cuerpo);
    }

    @Test
    void alIniciarUnIntentoSeFijanLasPreguntasSinMostrarLaRespuestaCorrecta() throws Exception {
        Escenario e = escenario(null);
        HttpResponse<String> respuesta = enviar("POST", rutaIntentos(e.evaluacionId()), ANA, null);

        assertEquals(201, respuesta.statusCode());
        assertTrue(respuesta.body().contains("\"estado\":\"EN_CURSO\""));
        assertFalse(respuesta.body().contains("esCorrecta"));
        int filas = jdbc.queryForObject(
                "SELECT count(*) FROM respuestas WHERE intento_id = ? AND opcion_id IS NULL AND es_correcta IS NULL",
                Integer.class, idDe(respuesta));
        assertEquals(3, filas);
    }

    @Test
    void iniciarDeNuevoRetomaElIntentoEnCurso() throws Exception {
        Escenario e = escenario(null);
        Long primero = iniciar(e);

        HttpResponse<String> segundo = enviar("POST", rutaIntentos(e.evaluacionId()), ANA, null);

        assertEquals(200, segundo.statusCode());
        assertEquals(primero, idDe(segundo));
        int intentos = jdbc.queryForObject("SELECT count(*) FROM intentos WHERE evaluacion_id = ?", Integer.class,
                e.evaluacionId());
        assertEquals(1, intentos);
        assertTrue(enviar("GET", "/api/v1/courses/" + e.cursoId() + "/quizzes", ANA, null).body()
                .contains("\"intentosUsados\":1"));
    }

    @Test
    void soloUnEstudianteMatriculadoIniciaIntentos() throws Exception {
        Escenario e = escenario(null);
        assertEquals(403, estado("POST", rutaIntentos(e.evaluacionId()), PEDRO, null));
        assertEquals(403, estado("POST", rutaIntentos(e.evaluacionId()), LAURA, null));
        assertEquals(401, estado("POST", rutaIntentos(e.evaluacionId()), null, null));
        assertEquals(404, estado("POST", rutaIntentos(999999999L), ANA, null));
    }

    @Test
    void seRespetaElLimiteDeIntentos() throws Exception {
        Escenario e = escenario(1);
        Long intentoId = iniciar(e);
        assertEquals(200, finalizar(intentoId, null).statusCode());
        assertEquals(409, estado("POST", rutaIntentos(e.evaluacionId()), ANA, null));
    }

    @Test
    void seRegistraYSeCambiaLaRespuesta() throws Exception {
        Escenario e = escenario(null);
        Long intentoId = iniciar(e);
        Long p1 = e.preguntas().get(0);

        assertEquals(200, responder(intentoId, p1, opcionIncorrecta(p1), ANA));
        assertEquals(200, responder(intentoId, p1, opcionCorrecta(p1), ANA));
        Long guardada = jdbc.queryForObject("SELECT opcion_id FROM respuestas WHERE intento_id = ? AND pregunta_id = ?",
                Long.class, intentoId, p1);
        assertEquals(opcionCorrecta(p1), guardada);

        assertEquals(400, responder(intentoId, p1, opcionCorrecta(e.preguntas().get(1)), ANA));
        assertEquals(400, responder(intentoId, 999999999L, opcionCorrecta(p1), ANA));
        assertEquals(404, responder(intentoId, p1, 999999999L, ANA));
        assertEquals(400, estado("POST", "/api/v1/attempts/" + intentoId + "/answers", ANA, "{}"));
    }

    @Test
    void otroEstudianteNoAccedeAlIntento() throws Exception {
        Escenario e = escenario(null);
        matricular(PEDRO, e.cursoId());
        Long intentoId = iniciar(e);
        Long p1 = e.preguntas().get(0);

        assertEquals(403, estado("GET", "/api/v1/attempts/" + intentoId, PEDRO, null));
        assertEquals(403, responder(intentoId, p1, opcionCorrecta(p1), PEDRO));
        assertEquals(403, estado("POST", "/api/v1/attempts/" + intentoId + "/submit", PEDRO, null));
        assertEquals(403, estado("GET", "/api/v1/attempts/" + intentoId + "/result", PEDRO, null));
    }

    @Test
    void laPuntuacionSeCalculaEnElServidorYSeIgnoraLaDelCliente() throws Exception {
        Escenario e = escenario(null);
        Long intentoId = iniciar(e);
        responder(intentoId, e.preguntas().get(0), opcionCorrecta(e.preguntas().get(0)), ANA);
        responder(intentoId, e.preguntas().get(1), opcionCorrecta(e.preguntas().get(1)), ANA);

        HttpResponse<String> respuesta = finalizar(intentoId, "{\"puntuacion\":100}");

        assertEquals(200, respuesta.statusCode());
        assertTrue(respuesta.body().contains("\"puntuacion\":66.67"));
        assertTrue(respuesta.body().contains("\"correctas\":2"));
        assertTrue(respuesta.body().contains("\"sinResponder\":1"));
        assertTrue(respuesta.body().contains("\"incorrectas\":0"));
        assertTrue(respuesta.body().contains("\"estado\":\"FINALIZADO\""));

        String estadoBd = jdbc.queryForObject("SELECT estado FROM intentos WHERE id = ?", String.class, intentoId);
        BigDecimal puntuacionBd = jdbc.queryForObject("SELECT puntuacion FROM intentos WHERE id = ?", BigDecimal.class,
                intentoId);
        int sinCalificar = jdbc.queryForObject(
                "SELECT count(*) FROM respuestas WHERE intento_id = ? AND es_correcta IS NULL", Integer.class, intentoId);
        assertEquals("FINALIZADO", estadoBd);
        assertEquals(0, puntuacionBd.compareTo(new BigDecimal("66.67")));
        assertEquals(0, sinCalificar);
    }

    @Test
    void laPuntuacionExtremaEsCeroOCien() throws Exception {
        Escenario e = escenario(null);
        Long perfecto = iniciar(e);
        for (Long pregunta : e.preguntas()) {
            responder(perfecto, pregunta, opcionCorrecta(pregunta), ANA);
        }
        assertTrue(finalizar(perfecto, null).body().contains("\"puntuacion\":100"));

        Long fallido = iniciar(e);
        for (Long pregunta : e.preguntas()) {
            responder(fallido, pregunta, opcionIncorrecta(pregunta), ANA);
        }
        HttpResponse<String> respuesta = finalizar(fallido, null);
        assertTrue(respuesta.body().contains("\"puntuacion\":0"));
        assertTrue(respuesta.body().contains("\"incorrectas\":3"));
    }

    @Test
    void unIntentoFinalizadoNoSeModifica() throws Exception {
        Escenario e = escenario(null);
        Long intentoId = iniciar(e);
        Long p1 = e.preguntas().get(0);
        finalizar(intentoId, null);

        assertEquals(409, responder(intentoId, p1, opcionCorrecta(p1), ANA));
        assertEquals(409, finalizar(intentoId, null).statusCode());
        assertThrows(DataAccessException.class,
                () -> jdbc.update("UPDATE intentos SET puntuacion = 100 WHERE id = ?", intentoId));
        assertThrows(DataAccessException.class, () -> jdbc.update("DELETE FROM intentos WHERE id = ?", intentoId));
        assertThrows(DataAccessException.class,
                () -> jdbc.update("UPDATE respuestas SET es_correcta = TRUE WHERE intento_id = ?", intentoId));
    }

    @Test
    void laRespuestaCorrectaSoloSeRevelaAlFinalizar() throws Exception {
        Escenario e = escenario(null);
        Long intentoId = iniciar(e);

        assertEquals(409, estado("GET", "/api/v1/attempts/" + intentoId + "/result", ANA, null));
        HttpResponse<String> enCurso = enviar("GET", "/api/v1/attempts/" + intentoId, ANA, null);
        assertEquals(200, enCurso.statusCode());
        assertFalse(enCurso.body().contains("esCorrecta"));

        finalizar(intentoId, null);

        HttpResponse<String> resultado = enviar("GET", "/api/v1/attempts/" + intentoId + "/result", ANA, null);
        assertEquals(200, resultado.statusCode());
        assertTrue(resultado.body().contains("\"esCorrecta\":true"));
        assertTrue(resultado.body().contains("Explicación de P1"));
        assertEquals(409, estado("GET", "/api/v1/attempts/" + intentoId, ANA, null));
    }

    @Test
    void elHistorialSoloMuestraLosIntentosPropios() throws Exception {
        Escenario e = escenario(null);
        Long intentoId = iniciar(e);

        assertTrue(enviar("GET", "/api/v1/attempts", ANA, null).body().contains("\"id\":" + intentoId));
        assertFalse(enviar("GET", "/api/v1/attempts", PEDRO, null).body().contains("\"id\":" + intentoId));
        assertEquals(403, estado("GET", "/api/v1/attempts", LAURA, null));
    }
}
