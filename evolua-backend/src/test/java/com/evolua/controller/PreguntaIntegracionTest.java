package com.evolua.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.net.http.HttpResponse;

import org.junit.jupiter.api.Test;

import com.evolua.entity.Rol;

/**
 * Pruebas del banco de preguntas: validación de opciones, permisos, edición, desactivación, regla de preguntas
 * con respuestas y auditoría.
 */
class PreguntaIntegracionTest extends PruebaApiBase {

    private static String opcion(String texto, boolean correcta) {
        return "{\"texto\":\"" + texto + "\",\"esCorrecta\":" + correcta + "}";
    }

    private static String pregunta(Long temaId, String enunciado, int dificultad, String tipo, String... opciones) {
        return "{\"temaId\":" + temaId + ",\"enunciado\":\"" + enunciado + "\",\"dificultad\":" + dificultad
                + ",\"tipo\":\"" + tipo + "\",\"retroalimentacion\":\"Explicación\",\"opciones\":["
                + String.join(",", opciones) + "]}";
    }

    private Long temaNuevo() throws Exception {
        return crearTema(crearCurso("Curso de preguntas"), "Tema de preguntas");
    }

    private Long preguntaValida(Long temaId, String enunciado) throws Exception {
        HttpResponse<String> respuesta = enviar("POST", "/api/v1/questions", LAURA,
                pregunta(temaId, enunciado, 2, "SELECCION_MULTIPLE", opcion("A", true), opcion("B", false), opcion("C", false)));
        assertEquals(201, respuesta.statusCode());
        return idDe(respuesta);
    }

    @Test
    void elDocenteCreaUnaPreguntaConSusOpciones() throws Exception {
        Long temaId = temaNuevo();
        HttpResponse<String> creada = enviar("POST", "/api/v1/questions", LAURA,
                pregunta(temaId, "¿Cuánto es 2 + 2?", 1, "SELECCION_MULTIPLE", opcion("3", false), opcion("4", true), opcion("5", false)));
        assertEquals(201, creada.statusCode());
        Long id = idDe(creada);

        HttpResponse<String> consulta = enviar("GET", "/api/v1/questions/" + id, LAURA, null);
        assertEquals(200, consulta.statusCode());
        assertTrue(consulta.body().contains("\"esCorrecta\":true"));
        int opciones = jdbc.queryForObject("SELECT count(*) FROM opciones_pregunta WHERE pregunta_id = ?", Integer.class, id);
        assertEquals(3, opciones);
    }

    @Test
    void unEstudianteNoAccedeAlBancoDePreguntas() throws Exception {
        Long temaId = temaNuevo();
        Long id = preguntaValida(temaId, "Pregunta reservada");
        assertEquals(403, estado("GET", "/api/v1/questions/" + id, ANA, null));
        assertEquals(403, estado("GET", "/api/v1/topics/" + temaId + "/questions", ANA, null));
        assertEquals(403, estado("POST", "/api/v1/questions", ANA,
                pregunta(temaId, "No permitido", 1, "SELECCION_MULTIPLE", opcion("A", true), opcion("B", false))));
    }

    @Test
    void unDocenteAjenoNoAccedeALasPreguntasDelTema() throws Exception {
        crearUsuario("docente.ajeno@uni.edu", Rol.DOCENTE);
        Long temaId = temaNuevo();
        Long id = preguntaValida(temaId, "Pregunta de otro docente");
        String ajeno = "docente.ajeno@uni.edu";
        assertEquals(403, estado("GET", "/api/v1/questions/" + id, ajeno, null));
        assertEquals(403, estado("DELETE", "/api/v1/questions/" + id, ajeno, null));
        assertEquals(403, estado("POST", "/api/v1/questions", ajeno,
                pregunta(temaId, "No permitido", 1, "SELECCION_MULTIPLE", opcion("A", true), opcion("B", false))));
    }

    @Test
    void lasOpcionesInvalidasSeRechazan() throws Exception {
        Long temaId = temaNuevo();
        assertEquals(400, estado("POST", "/api/v1/questions", LAURA,
                pregunta(temaId, "Dos correctas", 1, "SELECCION_MULTIPLE", opcion("A", true), opcion("B", true))));
        assertEquals(400, estado("POST", "/api/v1/questions", LAURA,
                pregunta(temaId, "Ninguna correcta", 1, "SELECCION_MULTIPLE", opcion("A", false), opcion("B", false))));
        assertEquals(400, estado("POST", "/api/v1/questions", LAURA,
                pregunta(temaId, "Una sola opción", 1, "SELECCION_MULTIPLE", opcion("A", true))));
        assertEquals(400, estado("POST", "/api/v1/questions", LAURA,
                pregunta(temaId, "Verdadero o falso con tres", 1, "VERDADERO_FALSO",
                        opcion("Verdadero", true), opcion("Falso", false), opcion("Quizá", false))));
        assertEquals(400, estado("POST", "/api/v1/questions", LAURA,
                pregunta(temaId, "Dificultad fuera de rango", 5, "SELECCION_MULTIPLE", opcion("A", true), opcion("B", false))));
        assertEquals(400, estado("POST", "/api/v1/questions", LAURA,
                pregunta(temaId, "Tipo desconocido", 1, "ABIERTA", opcion("A", true), opcion("B", false))));
    }

    @Test
    void unaPreguntaVerdaderoFalsoSeAcepta() throws Exception {
        Long temaId = temaNuevo();
        assertEquals(201, estado("POST", "/api/v1/questions", LAURA,
                pregunta(temaId, "Un boolean admite true o false", 1, "VERDADERO_FALSO",
                        opcion("Verdadero", true), opcion("Falso", false))));
    }

    @Test
    void elDocenteActualizaUnaPreguntaSinRespuestasYQuedaAuditado() throws Exception {
        Long temaId = temaNuevo();
        Long id = preguntaValida(temaId, "Enunciado original");
        HttpResponse<String> respuesta = enviar("PUT", "/api/v1/questions/" + id, LAURA,
                pregunta(temaId, "Enunciado nuevo", 3, "SELECCION_MULTIPLE", opcion("X", false), opcion("Y", true)));
        assertEquals(200, respuesta.statusCode());
        assertTrue(respuesta.body().contains("\"enunciado\":\"Enunciado nuevo\""));

        int opciones = jdbc.queryForObject("SELECT count(*) FROM opciones_pregunta WHERE pregunta_id = ?", Integer.class, id);
        assertEquals(2, opciones);
        int auditados = jdbc.queryForObject(
                "SELECT count(*) FROM auditoria WHERE entidad = 'preguntas' AND entidad_id = ? AND accion = 'UPDATE' "
                        + "AND usuario_id = (SELECT id FROM usuarios WHERE correo = ?)",
                Integer.class, id, LAURA);
        assertEquals(1, auditados);
    }

    @Test
    void unaPreguntaConRespuestasNoCambiaSuContenido() throws Exception {
        Long temaId = temaNuevo();
        String cuerpo = pregunta(temaId, "Pregunta con respuestas", 1, "SELECCION_MULTIPLE", opcion("A", true), opcion("B", false));
        Long id = idDe(enviar("POST", "/api/v1/questions", LAURA, cuerpo));
        jdbc.update("INSERT INTO intentos (estudiante_id, tema_id, tipo) SELECT id, ?::bigint, 'PRACTICA' FROM usuarios WHERE correo = ?",
                temaId, ANA);
        jdbc.update("INSERT INTO respuestas (intento_id, pregunta_id) VALUES ((SELECT max(id) FROM intentos), ?)", id);

        assertEquals(409, estado("PUT", "/api/v1/questions/" + id, LAURA,
                pregunta(temaId, "Otro enunciado", 1, "SELECCION_MULTIPLE", opcion("A", true), opcion("B", false))));
        assertEquals(409, estado("PUT", "/api/v1/questions/" + id, LAURA,
                pregunta(temaId, "Pregunta con respuestas", 1, "SELECCION_MULTIPLE", opcion("A", false), opcion("B", true))));
        assertEquals(200, estado("PUT", "/api/v1/questions/" + id, LAURA,
                cuerpo.replace("\"retroalimentacion\"", "\"activa\":false,\"retroalimentacion\"")));
    }

    @Test
    void desactivarUnaPreguntaNoLaElimina() throws Exception {
        Long temaId = temaNuevo();
        Long id = preguntaValida(temaId, "Pregunta a desactivar");
        assertEquals(204, estado("DELETE", "/api/v1/questions/" + id, LAURA, null));

        HttpResponse<String> consulta = enviar("GET", "/api/v1/questions/" + id, LAURA, null);
        assertEquals(200, consulta.statusCode());
        assertTrue(consulta.body().contains("\"activa\":false"));
        int filas = jdbc.queryForObject("SELECT count(*) FROM preguntas WHERE id = ?", Integer.class, id);
        assertEquals(1, filas);
    }

    @Test
    void sePuedenListarLasPreguntasDeUnTema() throws Exception {
        Long temaId = temaNuevo();
        Long primera = preguntaValida(temaId, "Primera");
        Long segunda = preguntaValida(temaId, "Segunda");
        HttpResponse<String> lista = enviar("GET", "/api/v1/topics/" + temaId + "/questions", LAURA, null);
        assertEquals(200, lista.statusCode());
        assertTrue(lista.body().contains("\"id\":" + primera));
        assertTrue(lista.body().contains("\"id\":" + segunda));
    }
}
