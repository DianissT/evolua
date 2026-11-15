package com.evolua.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

import com.evolua.entity.Rol;
import com.evolua.entity.Usuario;

/**
 * Pruebas de los reportes del docente. Cada indicador se contrasta con un cálculo independiente: una consulta SQL
 * directa o una cuenta hecha a mano sobre datos conocidos.
 */
class ReporteIntegracionTest extends PruebaApiBase {

    private static final String PEDRO = "pedro.soto@uni.edu";

    private record Escenario(Long cursoId, Long temaId, List<Long> preguntas, Long evaluacionId) {
    }

    /** Curso con un tema, cinco preguntas y una evaluación; Ana y Pedro quedan matriculados. */
    private Escenario escenario() throws Exception {
        Long cursoId = crearCurso("Curso de reportes");
        Long temaId = crearTema(cursoId, "Tema de reportes");
        List<Long> preguntas = new ArrayList<>();
        for (int i = 1; i <= 5; i++) {
            preguntas.add(crearPregunta(temaId, "Pregunta de reporte " + i));
        }
        Long evaluacionId = crearEvaluacion(cursoId, "Evaluación de reportes", null, preguntas.toArray(new Long[0]));
        matricular(ANA, cursoId);
        matricular(PEDRO, cursoId);
        return new Escenario(cursoId, temaId, preguntas, evaluacionId);
    }

    /** Ana resuelve la evaluación acertando las primeras {@code correctas} preguntas. */
    private void resolverComoAna(Escenario e, int correctas) throws Exception {
        HttpResponse<String> inicio = enviar("POST", "/api/v1/quizzes/" + e.evaluacionId() + "/attempts", ANA, null);
        assertEquals(201, inicio.statusCode());
        Long intentoId = idDe(inicio);
        for (int i = 0; i < e.preguntas().size(); i++) {
            Long pregunta = e.preguntas().get(i);
            Long opcion = i < correctas ? opcionCorrecta(pregunta) : opcionIncorrecta(pregunta);
            assertEquals(200, estado("POST", "/api/v1/attempts/" + intentoId + "/answers", ANA,
                    "{\"preguntaId\":" + pregunta + ",\"opcionId\":" + opcion + "}"));
        }
        assertEquals(200, estado("POST", "/api/v1/attempts/" + intentoId + "/submit", ANA, null));
    }

    private String rendimiento(Long cursoId) throws Exception {
        HttpResponse<String> respuesta = enviar("GET", "/api/v1/reports/courses/" + cursoId + "/performance", LAURA,
                null);
        assertEquals(200, respuesta.statusCode());
        return respuesta.body();
    }

    @Test
    void soloElDocenteDelCursoConsultaLosReportes() throws Exception {
        Escenario e = escenario();
        String ruta = "/api/v1/reports/courses/" + e.cursoId() + "/performance";
        String rutaDificiles = "/api/v1/reports/courses/" + e.cursoId() + "/hard-questions";
        crearUsuario("otro.docente@test.edu", Rol.DOCENTE);

        assertEquals(401, estado("GET", ruta, null, null));
        assertEquals(403, estado("GET", ruta, ANA, null));
        assertEquals(403, estado("GET", ruta, ADMIN, null));
        assertEquals(403, estado("GET", rutaDificiles, ANA, null));
        assertEquals(403, estado("GET", ruta, "otro.docente@test.edu", null));
        assertEquals(403, estado("GET", rutaDificiles, "otro.docente@test.edu", null));
        assertEquals(200, estado("GET", ruta, LAURA, null));
        assertEquals(404, estado("GET", "/api/v1/reports/courses/999999999/performance", LAURA, null));
    }

    @Test
    void unCursoSinActividadMuestraCerosYPromedioNulo() throws Exception {
        Escenario e = escenario();

        String cuerpo = rendimiento(e.cursoId());

        assertTrue(cuerpo.contains("\"estudiantesMatriculados\":2"));
        assertTrue(cuerpo.contains("\"estudiantesConActividad\":0"));
        assertTrue(cuerpo.contains("\"intentosFinalizados\":0"));
        assertTrue(cuerpo.contains("\"promedioPuntuacion\":null"));
        assertTrue(cuerpo.contains("\"totalRespuestas\":0"));
    }

    @Test
    void losIndicadoresCoincidenConUnCalculoManual() throws Exception {
        Escenario e = escenario();
        resolverComoAna(e, 2); // 2 de 5 = 40 %
        resolverComoAna(e, 5); // 5 de 5 = 100 %

        String cuerpo = rendimiento(e.cursoId());

        // A mano: 10 respuestas, 7 correctas, 70 %; promedio de los dos intentos (40 + 100) / 2 = 70.
        assertTrue(cuerpo.contains("\"estudiantesConActividad\":1"));
        assertTrue(cuerpo.contains("\"intentosFinalizados\":2"));
        assertTrue(cuerpo.contains("\"totalRespuestas\":10"));
        assertTrue(cuerpo.contains("\"respuestasCorrectas\":7"));
        assertTrue(cuerpo.contains("\"porcentajeAciertos\":70.00"));

        // Contraste con SQL directo sobre las mismas tablas.
        BigDecimal promedioSql = jdbc.queryForObject("SELECT round(avg(i.puntuacion), 2) FROM intentos i "
                + "JOIN evaluaciones ev ON ev.id = i.evaluacion_id WHERE ev.curso_id = ? AND i.estado = 'FINALIZADO'",
                BigDecimal.class, e.cursoId());
        assertEquals(0, promedioSql.compareTo(new BigDecimal("70.00")));
        assertTrue(cuerpo.contains("\"promedioPuntuacion\":" + promedioSql.toPlainString()));
    }

    @Test
    void elReporteListaALosMatriculadosSinActividad() throws Exception {
        Escenario e = escenario();
        resolverComoAna(e, 3);

        String cuerpo = rendimiento(e.cursoId());

        assertTrue(cuerpo.contains("\"correo\":\"" + PEDRO + "\""));
        int posicionPedro = cuerpo.indexOf("\"correo\":\"" + PEDRO + "\"");
        String filaPedro = cuerpo.substring(posicionPedro, Math.min(cuerpo.length(), posicionPedro + 200));
        assertTrue(filaPedro.contains("\"intentosFinalizados\":0"));
        assertTrue(filaPedro.contains("\"promedioPuntuacion\":null"));
    }

    @Test
    void losEstudiantesEnRefuerzoSeCuentanPorTemaYPorEstudiante() throws Exception {
        Escenario e = escenario();
        resolverComoAna(e, 2); // 40 % con 5 respuestas: el tema queda en REFUERZO

        String cuerpo = rendimiento(e.cursoId());

        assertEquals("REFUERZO", jdbc.queryForObject("SELECT nivel FROM progreso_tema WHERE tema_id = ?",
                String.class, e.temaId()));
        assertTrue(cuerpo.contains("\"estudiantesEnRefuerzo\":1"));
        assertTrue(cuerpo.contains("\"temasEnRefuerzo\":1"));
    }

    @Test
    void laPracticaTambienCuentaEnElRendimientoDelCurso() throws Exception {
        Escenario e = escenario();
        HttpResponse<String> inicio = enviar("POST", "/api/v1/topics/" + e.temaId() + "/practice", ANA,
                "{\"modalidad\":\"LIBRE\",\"cantidad\":5}");
        assertEquals(201, inicio.statusCode());
        Long intentoId = idDe(inicio);
        assertEquals(200, estado("POST", "/api/v1/attempts/" + intentoId + "/submit", ANA, null));

        String cuerpo = rendimiento(e.cursoId());

        assertTrue(cuerpo.contains("\"estudiantesConActividad\":1"));
        assertTrue(cuerpo.contains("\"intentosFinalizados\":1"));
    }

    @Test
    void lasPreguntasDificilesSeOrdenanPorPorcentajeDeAciertos() throws Exception {
        Escenario e = escenario();
        for (int i = 0; i < 3; i++) {
            resolverComoAna(e, 2); // preguntas 1 y 2 siempre bien; 3, 4 y 5 siempre mal
        }
        String ruta = "/api/v1/reports/courses/" + e.cursoId() + "/hard-questions";

        HttpResponse<String> respuesta = enviar("GET", ruta, LAURA, null);

        assertEquals(200, respuesta.statusCode());
        String cuerpo = respuesta.body();
        int dificil = cuerpo.indexOf("\"preguntaId\":" + e.preguntas().get(2));
        int facil = cuerpo.indexOf("\"preguntaId\":" + e.preguntas().get(0));
        assertTrue(dificil >= 0 && facil >= 0 && dificil < facil);
        assertTrue(cuerpo.contains("\"porcentajeAciertos\":0.00"));
        assertTrue(cuerpo.contains("\"porcentajeAciertos\":100.00"));
        assertEquals("[]", enviar("GET", ruta + "?min=4", LAURA, null).body());
        assertEquals("[]", enviar("GET", ruta + "?temaId=999999999", LAURA, null).body());
        assertTrue(enviar("GET", ruta + "?temaId=" + e.temaId(), LAURA, null).body()
                .contains("\"preguntaId\":" + e.preguntas().get(2)));
        assertEquals(400, estado("GET", ruta + "?min=0", LAURA, null));
    }
}
