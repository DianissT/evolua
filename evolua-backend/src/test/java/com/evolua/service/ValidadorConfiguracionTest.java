package com.evolua.service;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.Test;

/**
 * Reglas de los parámetros de configuración. No usan Spring ni base de datos.
 */
class ValidadorConfiguracionTest {

    private static Map<String, String> porDefecto() {
        Map<String, String> valores = new HashMap<>();
        valores.put(ConfiguracionService.UMBRAL_REFUERZO, "60");
        valores.put(ConfiguracionService.UMBRAL_DOMINIO, "80");
        valores.put(ConfiguracionService.MIN_RESPUESTAS_TEMA, "5");
        valores.put(ConfiguracionService.INTENTOS_PARA_DOMINIO, "2");
        valores.put(ConfiguracionService.PREGUNTAS_POR_PRACTICA, "5");
        return valores;
    }

    private static IllegalArgumentException rechazo(String clave, String valor) {
        Map<String, String> valores = porDefecto();
        valores.put(clave, valor);
        return assertThrows(IllegalArgumentException.class, () -> ValidadorConfiguracion.validar(valores));
    }

    @Test
    void losValoresDelSeedSonValidos() {
        assertDoesNotThrow(() -> ValidadorConfiguracion.validar(porDefecto()));
    }

    @Test
    void elUmbralDeRefuerzoDebeSerMenorQueElDeDominio() {
        assertTrue(rechazo(ConfiguracionService.UMBRAL_REFUERZO, "80").getMessage().contains("menor"));
        assertTrue(rechazo(ConfiguracionService.UMBRAL_REFUERZO, "85").getMessage().contains("menor"));
        assertTrue(rechazo(ConfiguracionService.UMBRAL_DOMINIO, "60").getMessage().contains("menor"));
        assertDoesNotThrow(() -> {
            Map<String, String> valores = porDefecto();
            valores.put(ConfiguracionService.UMBRAL_REFUERZO, "79");
            ValidadorConfiguracion.validar(valores);
        });
    }

    @Test
    void losUmbralesEstanDentroDeSuRango() {
        rechazo(ConfiguracionService.UMBRAL_REFUERZO, "-1");
        rechazo(ConfiguracionService.UMBRAL_REFUERZO, "100");
        rechazo(ConfiguracionService.UMBRAL_DOMINIO, "101");
        rechazo(ConfiguracionService.UMBRAL_DOMINIO, "0");
    }

    @Test
    void losDemasParametrosTienenLimites() {
        rechazo(ConfiguracionService.MIN_RESPUESTAS_TEMA, "0");
        rechazo(ConfiguracionService.INTENTOS_PARA_DOMINIO, "0");
        rechazo(ConfiguracionService.INTENTOS_PARA_DOMINIO, "11");
        rechazo(ConfiguracionService.PREGUNTAS_POR_PRACTICA, "0");
        rechazo(ConfiguracionService.PREGUNTAS_POR_PRACTICA, "21");
    }

    @Test
    void unValorQueNoEsEnteroSeRechaza() {
        assertTrue(rechazo(ConfiguracionService.UMBRAL_DOMINIO, "abc").getMessage().contains("entero"));
        rechazo(ConfiguracionService.MIN_RESPUESTAS_TEMA, "2.5");
        rechazo(ConfiguracionService.PREGUNTAS_POR_PRACTICA, "");
    }

    @Test
    void losEspaciosAlrededorDelNumeroSeIgnoran() {
        assertDoesNotThrow(() -> {
            Map<String, String> valores = porDefecto();
            valores.put(ConfiguracionService.UMBRAL_DOMINIO, " 90 ");
            ValidadorConfiguracion.validar(valores);
        });
    }

    @Test
    void losParametrosAusentesNoSeValidan() {
        assertDoesNotThrow(() -> ValidadorConfiguracion.validar(Map.of(ConfiguracionService.UMBRAL_DOMINIO, "90")));
    }
}
