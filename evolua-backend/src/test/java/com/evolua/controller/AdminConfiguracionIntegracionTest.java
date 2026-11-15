package com.evolua.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.net.http.HttpResponse;
import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Pruebas de la edición de umbrales.
 */
class AdminConfiguracionIntegracionTest extends PruebaApiBase {

    private static final String BASE = "/api/v1/admin/config";

    private final Map<String, String> originales = new HashMap<>();

    @BeforeEach
    void guardarValoresOriginales() {
        originales.clear();
        for (Map<String, Object> fila : jdbc.queryForList("SELECT clave, valor FROM configuracion")) {
            originales.put((String) fila.get("clave"), (String) fila.get("valor"));
        }
    }

    @AfterEach
    void restaurarValoresOriginales() {
        originales.forEach((clave, valor) -> jdbc.update("UPDATE configuracion SET valor = ? WHERE clave = ?", valor,
                clave));
    }

    private String valor(String clave) {
        return jdbc.queryForObject("SELECT valor FROM configuracion WHERE clave = ?", String.class, clave);
    }

    private int cambiar(String clave, String valor) throws Exception {
        return estado("PUT", BASE + "/" + clave, ADMIN, "{\"valor\":\"" + valor + "\"}");
    }

    @Test
    void soloElAdministradorConsultaLaConfiguracion() throws Exception {
        assertEquals(401, estado("GET", BASE, null, null));
        assertEquals(403, estado("GET", BASE, ANA, null));
        assertEquals(403, estado("GET", BASE, LAURA, null));

        HttpResponse<String> respuesta = enviar("GET", BASE, ADMIN, null);
        assertEquals(200, respuesta.statusCode());
        for (String clave : originales.keySet()) {
            assertTrue(respuesta.body().contains("\"clave\":\"" + clave + "\""));
        }
    }

    @Test
    void elCambioDeUnUmbralSeGuardaYSeAudita() throws Exception {
        assertEquals(403, estado("PUT", BASE + "/umbral_refuerzo", LAURA, "{\"valor\":\"70\"}"));

        HttpResponse<String> respuesta = enviar("PUT", BASE + "/umbral_refuerzo", ADMIN, "{\"valor\":\"70\"}");

        assertEquals(200, respuesta.statusCode());
        assertEquals("70", valor("umbral_refuerzo"));
        String detalle = jdbc.queryForObject("SELECT detalle FROM auditoria WHERE accion = 'ACTUALIZAR' "
                + "AND entidad = 'configuracion' ORDER BY id DESC LIMIT 1", String.class);
        assertEquals("umbral_refuerzo: " + originales.get("umbral_refuerzo") + " -> 70", detalle);
    }

    @Test
    void losUmbralesIncoherentesSeRechazanYNoCambianNada() throws Exception {
        assertEquals(400, cambiar("umbral_refuerzo", originales.get("umbral_dominio")));
        assertEquals(400, cambiar("umbral_dominio", originales.get("umbral_refuerzo")));
        assertEquals(originales.get("umbral_refuerzo"), valor("umbral_refuerzo"));
        assertEquals(originales.get("umbral_dominio"), valor("umbral_dominio"));
    }

    @Test
    void losValoresInvalidosDevuelven400() throws Exception {
        assertEquals(400, cambiar("umbral_dominio", "abc"));
        assertEquals(400, cambiar("umbral_refuerzo", "-5"));
        assertEquals(400, cambiar("min_respuestas_tema", "0"));
        assertEquals(400, cambiar("preguntas_por_practica", "21"));
        assertEquals(400, estado("PUT", BASE + "/umbral_dominio", ADMIN, "{\"valor\":\"\"}"));
        assertEquals(originales.get("preguntas_por_practica"), valor("preguntas_por_practica"));
    }

    @Test
    void unParametroInexistenteDevuelve404() throws Exception {
        assertEquals(404, cambiar("no_existe", "1"));
    }

    @Test
    void repetirElMismoValorNoGeneraAuditoria() throws Exception {
        int antes = jdbc.queryForObject("SELECT count(*) FROM auditoria WHERE entidad = 'configuracion'",
                Integer.class);
        assertEquals(200, cambiar("min_respuestas_tema", originales.get("min_respuestas_tema")));
        int despues = jdbc.queryForObject("SELECT count(*) FROM auditoria WHERE entidad = 'configuracion'",
                Integer.class);
        assertEquals(antes, despues);
    }
}
