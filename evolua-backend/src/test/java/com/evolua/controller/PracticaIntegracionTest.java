package com.evolua.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.net.http.HttpResponse;
import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.Test;

/**
 * Pruebas de la práctica por tema: selección automática y libre, retomar la práctica en curso, calificación,
 * actualización del progreso y control de acceso.
 */
class PracticaIntegracionTest extends PruebaApiBase {

    private static final String PEDRO = "pedro.soto@uni.edu";
    private static final String AUTOMATICA = "{\"modalidad\":\"AUTOMATICA\"}";

    private record Escenario(Long cursoId, Long temaId, List<Long> basicas, List<Long> medias, List<Long> altas) {
    }

    private Escenario escenario() throws Exception {
        Long cursoId = crearCurso("Curso de práctica");
        Long temaId = crearTema(cursoId, "Tema de práctica");
        List<Long> basicas = List.of(crearPregunta(temaId, "B1", 1), crearPregunta(temaId, "B2", 1));
        List<Long> medias = List.of(crearPregunta(temaId, "M1", 2), crearPregunta(temaId, "M2", 2));
        List<Long> altas = List.of(crearPregunta(temaId, "A1", 3), crearPregunta(temaId, "A2", 3));
        matricular(ANA, cursoId);
        return new Escenario(cursoId, temaId, basicas, medias, altas);
    }

    private static String ruta(Long temaId) {
        return "/api/v1/topics/" + temaId + "/practice";
    }

    private static String libre(Integer dificultad, Integer cantidad) {
        return "{\"modalidad\":\"LIBRE\",\"dificultad\":" + dificultad + ",\"cantidad\":" + cantidad + "}";
    }

    /** Preguntas del intento en el orden en que se eligieron. */
    private List<Long> preguntasDe(Long intentoId) {
        return jdbc.queryForList("SELECT pregunta_id FROM respuestas WHERE intento_id = ? ORDER BY id", Long.class,
                intentoId);
    }

    private int preguntasPorPractica() {
        List<String> valores = jdbc.queryForList("SELECT valor FROM configuracion WHERE clave = 'preguntas_por_practica'",
                String.class);
        return valores.isEmpty() ? 5 : Integer.parseInt(valores.get(0).trim());
    }

    private HttpResponse<String> iniciar(Long temaId, String cuerpo) throws Exception {
        return enviar("POST", ruta(temaId), ANA, cuerpo);
    }

    private HttpResponse<String> finalizar(Long intentoId) throws Exception {
        return enviar("POST", "/api/v1/attempts/" + intentoId + "/submit", ANA, null);
    }

    @Test
    void laPracticaAutomaticaSinHistorialUsaDificultadMediaYCompletaConLasMasCercanas() throws Exception {
        Escenario e = escenario();

        HttpResponse<String> respuesta = iniciar(e.temaId(), null);

        assertEquals(201, respuesta.statusCode());
        assertTrue(respuesta.body().contains("\"tipo\":\"PRACTICA\""));
        assertTrue(respuesta.body().contains("\"evaluacionId\":null"));
        assertFalse(respuesta.body().contains("esCorrecta"));
        List<Long> elegidas = preguntasDe(idDe(respuesta));
        assertEquals(Math.min(6, preguntasPorPractica()), elegidas.size());
        assertEquals(Set.copyOf(e.medias().subList(0, Math.min(2, elegidas.size()))),
                Set.copyOf(elegidas.subList(0, Math.min(2, elegidas.size()))));
    }

    @Test
    void laPracticaAutomaticaSigueLaRecomendacionVigente() throws Exception {
        Escenario e = escenario();
        jdbc.update("INSERT INTO recomendaciones (estudiante_id, tema_id, tipo_actividad, motivo) "
                + "SELECT id, ?::bigint, 'AVANZADO', 'Prueba' FROM usuarios WHERE correo = ?", e.temaId(), ANA);

        HttpResponse<String> respuesta = iniciar(e.temaId(), AUTOMATICA);

        assertEquals(201, respuesta.statusCode());
        List<Long> elegidas = preguntasDe(idDe(respuesta));
        assertEquals(Set.copyOf(e.altas().subList(0, Math.min(2, elegidas.size()))),
                Set.copyOf(elegidas.subList(0, Math.min(2, elegidas.size()))));
    }

    @Test
    void laPracticaLibreRespetaLaDificultadElegida() throws Exception {
        Escenario e = escenario();

        HttpResponse<String> respuesta = iniciar(e.temaId(), libre(3, 5));

        assertEquals(201, respuesta.statusCode());
        assertEquals(Set.copyOf(e.altas()), Set.copyOf(preguntasDe(idDe(respuesta))));
        assertEquals(2, preguntasDe(idDe(respuesta)).size());
    }

    @Test
    void laPracticaLibreSinDificultadMezclaTodasYRespetaLaCantidad() throws Exception {
        Escenario e = escenario();

        HttpResponse<String> respuesta = iniciar(e.temaId(), "{\"modalidad\":\"LIBRE\",\"cantidad\":6}");

        assertEquals(201, respuesta.statusCode());
        assertEquals(6, preguntasDe(idDe(respuesta)).size());
    }

    @Test
    void laPracticaLibreFallaSiNoHayPreguntasDeLaDificultadElegida() throws Exception {
        Long cursoId = crearCurso("Curso solo básico");
        Long temaId = crearTema(cursoId, "Tema solo básico");
        crearPregunta(temaId, "B1", 1);
        matricular(ANA, cursoId);

        assertEquals(409, iniciar(temaId, libre(3, 5)).statusCode());
        assertEquals(201, iniciar(temaId, libre(1, 5)).statusCode());
    }

    @Test
    void laPracticaPriorizaLasPreguntasQueElEstudianteVeHaceMasTiempo() throws Exception {
        Escenario e = escenario();

        HttpResponse<String> primera = iniciar(e.temaId(), libre(1, 1));
        assertEquals(e.basicas().get(0), preguntasDe(idDe(primera)).get(0));
        assertEquals(200, finalizar(idDe(primera)).statusCode());

        HttpResponse<String> segunda = iniciar(e.temaId(), libre(1, 1));
        assertEquals(201, segunda.statusCode());
        assertEquals(e.basicas().get(1), preguntasDe(idDe(segunda)).get(0));
    }

    @Test
    void iniciarDeNuevoRetomaLaPracticaEnCurso() throws Exception {
        Escenario e = escenario();
        Long primera = idDe(iniciar(e.temaId(), AUTOMATICA));

        HttpResponse<String> segunda = iniciar(e.temaId(), libre(3, 2));

        assertEquals(200, segunda.statusCode());
        assertEquals(primera, idDe(segunda));
        int practicas = jdbc.queryForObject("SELECT count(*) FROM intentos WHERE tema_id = ? AND tipo = 'PRACTICA'",
                Integer.class, e.temaId());
        assertEquals(1, practicas);
    }

    @Test
    void finalizarUnaPracticaLaCalificaYActualizaElProgreso() throws Exception {
        Escenario e = escenario();
        HttpResponse<String> inicio = iniciar(e.temaId(), libre(1, 2));
        Long intentoId = idDe(inicio);
        for (Long preguntaId : preguntasDe(intentoId)) {
            assertEquals(200, estado("POST", "/api/v1/attempts/" + intentoId + "/answers", ANA,
                    "{\"preguntaId\":" + preguntaId + ",\"opcionId\":" + opcionCorrecta(preguntaId) + "}"));
        }

        HttpResponse<String> resultado = finalizar(intentoId);

        assertEquals(200, resultado.statusCode());
        assertTrue(resultado.body().contains("\"tipo\":\"PRACTICA\""));
        assertTrue(resultado.body().contains("\"correctas\":2"));
        assertTrue(resultado.body().contains("\"puntuacion\":100"));
        assertEquals(1, jdbc.queryForObject("SELECT count(*) FROM progreso_tema WHERE tema_id = ?", Integer.class,
                e.temaId()));
        assertEquals(200, estado("GET", "/api/v1/attempts/" + intentoId + "/result", ANA, null));
        assertTrue(enviar("GET", "/api/v1/attempts", ANA, null).body().contains("\"temaNombre\":\"Tema de práctica\""));
    }

    @Test
    void soloUnEstudianteMatriculadoPractica() throws Exception {
        Escenario e = escenario();

        assertEquals(403, estado("POST", ruta(e.temaId()), PEDRO, null));
        assertEquals(403, estado("POST", ruta(e.temaId()), LAURA, null));
        assertEquals(401, estado("POST", ruta(e.temaId()), null, null));
        assertEquals(404, estado("POST", ruta(999999999L), ANA, null));
    }

    @Test
    void noSePuedePracticarUnTemaInactivo() throws Exception {
        Escenario e = escenario();
        jdbc.update("UPDATE temas SET activo = false WHERE id = ?", e.temaId());

        assertEquals(409, iniciar(e.temaId(), null).statusCode());
    }

    @Test
    void seRechazanLasSolicitudesInvalidas() throws Exception {
        Escenario e = escenario();

        assertEquals(400, iniciar(e.temaId(), "{\"modalidad\":\"AUTOMATICA\",\"dificultad\":2}").statusCode());
        assertEquals(400, iniciar(e.temaId(), "{\"modalidad\":\"AUTOMATICA\",\"cantidad\":3}").statusCode());
        assertEquals(400, iniciar(e.temaId(), libre(4, 5)).statusCode());
        assertEquals(400, iniciar(e.temaId(), libre(2, 0)).statusCode());
        assertEquals(400, iniciar(e.temaId(), "{}").statusCode());
        assertEquals(400, iniciar(e.temaId(), "{\"modalidad\":\"OTRA\"}").statusCode());
        assertEquals(0, jdbc.queryForObject("SELECT count(*) FROM intentos WHERE tema_id = ?", Integer.class,
                e.temaId()));
    }
}
