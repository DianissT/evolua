package com.evolua.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

/**
 * Pruebas del motor adaptativo de extremo a extremo: al finalizar un intento se actualiza el progreso por tema y
 * se genera una recomendación explicable.
 */
class MotorAdaptativoIntegracionTest extends PruebaApiBase {

    private static final String PEDRO = "pedro.soto@uni.edu";

    private record Escenario(Long cursoId, Long temaId, List<Long> preguntas, Long evaluacionId) {
    }

    private Escenario escenario(int numeroPreguntas) throws Exception {
        Long cursoId = crearCurso("Curso del motor");
        Long temaId = crearTema(cursoId, "Tema del motor");
        List<Long> preguntas = new ArrayList<>();
        for (int i = 1; i <= numeroPreguntas; i++) {
            preguntas.add(crearPregunta(temaId, "Pregunta " + i));
        }
        Long evaluacionId = crearEvaluacion(cursoId, "Evaluación del motor", null, preguntas.toArray(new Long[0]));
        matricular(ANA, cursoId);
        return new Escenario(cursoId, temaId, preguntas, evaluacionId);
    }

    private HttpResponse<String> resolver(Long evaluacionId, List<Long> preguntas, int correctas) throws Exception {
        HttpResponse<String> inicio = enviar("POST", "/api/v1/quizzes/" + evaluacionId + "/attempts", ANA, null);
        assertEquals(201, inicio.statusCode());
        Long intentoId = idDe(inicio);
        for (int i = 0; i < preguntas.size(); i++) {
            Long pregunta = preguntas.get(i);
            Long opcion = i < correctas ? opcionCorrecta(pregunta) : opcionIncorrecta(pregunta);
            assertEquals(200, estado("POST", "/api/v1/attempts/" + intentoId + "/answers", ANA,
                    "{\"preguntaId\":" + pregunta + ",\"opcionId\":" + opcion + "}"));
        }
        HttpResponse<String> resultado = enviar("POST", "/api/v1/attempts/" + intentoId + "/submit", ANA, null);
        assertEquals(200, resultado.statusCode());
        return resultado;
    }

    private HttpResponse<String> resolver(Escenario escenario, int correctas) throws Exception {
        return resolver(escenario.evaluacionId(), escenario.preguntas(), correctas);
    }

    private String nivel(Long temaId) {
        return jdbc.queryForObject("SELECT nivel FROM progreso_tema WHERE tema_id = ? "
                + "AND estudiante_id = (SELECT id FROM usuarios WHERE correo = ?)", String.class, temaId, ANA);
    }

    private String actividadVigente(Long temaId) {
        return jdbc.queryForObject("SELECT tipo_actividad FROM recomendaciones WHERE tema_id = ? AND vigente "
                + "AND estudiante_id = (SELECT id FROM usuarios WHERE correo = ?)", String.class, temaId, ANA);
    }

    private int contarRecomendaciones(Long temaId, boolean soloVigentes) {
        return jdbc.queryForObject("SELECT count(*) FROM recomendaciones WHERE tema_id = ? "
                + "AND estudiante_id = (SELECT id FROM usuarios WHERE correo = ?)"
                + (soloVigentes ? " AND vigente" : ""), Integer.class, temaId, ANA);
    }

    private static String fragmento(String cuerpo, String marca) {
        int inicio = cuerpo.indexOf(marca);
        assertTrue(inicio >= 0);
        return cuerpo.substring(inicio, Math.min(cuerpo.length(), inicio + 260));
    }

    @Test
    void unEstudianteQueMejoraAvanzaDeRefuerzoADominado() throws Exception {
        Escenario e = escenario(5);

        HttpResponse<String> primero = resolver(e, 2);
        assertTrue(primero.body().contains("\"tipoActividad\":\"REFUERZO\""));
        assertTrue(primero.body().contains("\"porcentaje\":40.00"));
        assertEquals("REFUERZO", nivel(e.temaId()));

        resolver(e, 5);
        assertEquals("INTERMEDIO", nivel(e.temaId()));
        assertEquals("PRACTICA_ADICIONAL", actividadVigente(e.temaId()));
        assertEquals(1, contarRecomendaciones(e.temaId(), true));
        assertEquals(2, contarRecomendaciones(e.temaId(), false));

        resolver(e, 5);
        assertEquals("DOMINADO", nivel(e.temaId()));
        assertEquals("AVANZADO", actividadVigente(e.temaId()));
        assertEquals(1, contarRecomendaciones(e.temaId(), true));
        assertEquals(3, contarRecomendaciones(e.temaId(), false));

        int total = jdbc.queryForObject("SELECT total_respuestas FROM progreso_tema WHERE tema_id = ?", Integer.class,
                e.temaId());
        int correctas = jdbc.queryForObject("SELECT respuestas_correctas FROM progreso_tema WHERE tema_id = ?",
                Integer.class, e.temaId());
        int satisfactorios = jdbc.queryForObject("SELECT intentos_satisfactorios FROM progreso_tema WHERE tema_id = ?",
                Integer.class, e.temaId());
        BigDecimal porcentaje = jdbc.queryForObject("SELECT porcentaje_aciertos FROM progreso_tema WHERE tema_id = ?",
                BigDecimal.class, e.temaId());
        assertEquals(15, total);
        assertEquals(12, correctas);
        assertEquals(2, satisfactorios);
        assertEquals(0, porcentaje.compareTo(new BigDecimal("80.00")));

        HttpResponse<String> recomendaciones = enviar("GET", "/api/v1/recommendations/me", ANA, null);
        assertEquals(200, recomendaciones.statusCode());
        String propia = fragmento(recomendaciones.body(), "\"temaId\":" + e.temaId());
        assertTrue(propia.contains("\"dificultadMinima\":3"));
        assertTrue(propia.contains("Dominas Tema del motor"));
    }

    @Test
    void unUnicoResultadoAltoNoDeclaraElTemaComoDominado() throws Exception {
        Escenario e = escenario(5);

        HttpResponse<String> resultado = resolver(e, 5);

        assertEquals("AVANZADO", nivel(e.temaId()));
        assertEquals("CONFIRMACION", actividadVigente(e.temaId()));
        assertTrue(resultado.body().contains("\"dificultadMinima\":2"));
        assertTrue(resultado.body().contains("\"dificultadMaxima\":3"));
        int satisfactorios = jdbc.queryForObject("SELECT intentos_satisfactorios FROM progreso_tema WHERE tema_id = ?",
                Integer.class, e.temaId());
        assertEquals(1, satisfactorios);
    }

    @Test
    void conPocasRespuestasElTemaQuedaSinDatos() throws Exception {
        Escenario e = escenario(3);

        HttpResponse<String> resultado = resolver(e, 3);

        assertEquals("SIN_DATOS", nivel(e.temaId()));
        assertEquals("PRACTICA_ADICIONAL", actividadVigente(e.temaId()));
        assertTrue(resultado.body().contains("3 de 5 necesarias"));
    }

    @Test
    void losUmbralesSeLeenDeLaConfiguracion() throws Exception {
        Escenario e = escenario(5);
        String original = jdbc.queryForObject("SELECT valor FROM configuracion WHERE clave = 'umbral_refuerzo'",
                String.class);
        jdbc.update("UPDATE configuracion SET valor = '30' WHERE clave = 'umbral_refuerzo'");
        try {
            resolver(e, 2);
            assertEquals("INTERMEDIO", nivel(e.temaId()));
        } finally {
            jdbc.update("UPDATE configuracion SET valor = ? WHERE clave = 'umbral_refuerzo'", original);
        }
    }

    @Test
    void unIntentoConVariosTemasActualizaCadaUno() throws Exception {
        Long cursoId = crearCurso("Curso con dos temas");
        Long temaA = crearTema(cursoId, "Tema A");
        Long temaB = crearTema(cursoId, "Tema B");
        List<Long> preguntas = new ArrayList<>();
        for (int i = 1; i <= 3; i++) {
            preguntas.add(crearPregunta(temaA, "A" + i));
        }
        for (int i = 1; i <= 2; i++) {
            preguntas.add(crearPregunta(temaB, "B" + i));
        }
        Long evaluacionId = crearEvaluacion(cursoId, "Evaluación de dos temas", null, preguntas.toArray(new Long[0]));
        matricular(ANA, cursoId);

        HttpResponse<String> resultado = resolver(evaluacionId, preguntas, 3);

        assertTrue(resultado.body().contains("\"correctas\":3,\"total\":3"));
        assertTrue(resultado.body().contains("\"correctas\":0,\"total\":2"));
        assertEquals("SIN_DATOS", nivel(temaA));
        assertEquals("SIN_DATOS", nivel(temaB));
        assertEquals(1, contarRecomendaciones(temaA, true));
        assertEquals(1, contarRecomendaciones(temaB, true));
    }

    @Test
    void elProgresoIncluyeLosTemasSinIntentos() throws Exception {
        Escenario e = escenario(5);
        crearTema(e.cursoId(), "Tema sin intentos");
        resolver(e, 2);

        String cuerpo = enviar("GET", "/api/v1/progress/me", ANA, null).body();

        assertTrue(fragmento(cuerpo, "\"temaNombre\":\"Tema del motor\"").contains("\"nivel\":\"REFUERZO\""));
        String sinIntentos = fragmento(cuerpo, "\"temaNombre\":\"Tema sin intentos\"");
        assertTrue(sinIntentos.contains("\"totalRespuestas\":0"));
        assertTrue(sinIntentos.contains("\"nivel\":\"SIN_DATOS\""));
    }

    @Test
    void cadaEstudianteSoloVeSuProgresoYSusRecomendaciones() throws Exception {
        Escenario e = escenario(5);
        resolver(e, 2);

        assertFalse(enviar("GET", "/api/v1/progress/me", PEDRO, null).body().contains("Tema del motor"));
        assertFalse(enviar("GET", "/api/v1/recommendations/me", PEDRO, null).body().contains("Tema del motor"));
        assertEquals(403, estado("GET", "/api/v1/progress/me", LAURA, null));
        assertEquals(403, estado("GET", "/api/v1/recommendations/me", LAURA, null));
        assertEquals(401, estado("GET", "/api/v1/progress/me", null, null));
    }

    @Test
    void finalizarDosVecesNoDuplicaElProgreso() throws Exception {
        Escenario e = escenario(5);
        HttpResponse<String> resultado = resolver(e, 2);

        assertEquals(409, estado("POST", "/api/v1/attempts/" + idDe(resultado) + "/submit", ANA, null));

        int total = jdbc.queryForObject("SELECT total_respuestas FROM progreso_tema WHERE tema_id = ?", Integer.class,
                e.temaId());
        assertEquals(5, total);
        assertEquals(1, contarRecomendaciones(e.temaId(), false));
    }

    @Test
    void elResultadoConsultadoMasTardeIncluyeLasRecomendacionesVigentes() throws Exception {
        Escenario e = escenario(5);
        Long intentoId = idDe(resolver(e, 2));

        HttpResponse<String> resultado = enviar("GET", "/api/v1/attempts/" + intentoId + "/result", ANA, null);

        assertEquals(200, resultado.statusCode());
        assertTrue(resultado.body().contains("\"tipoActividad\":\"REFUERZO\""));
        assertTrue(resultado.body().contains("\"temas\":[{"));
    }
}
