package com.evolua.service;

import java.util.Map;

/**
 * Reglas de los parámetros de configuración. Es una clase Java normal, sin dependencias de Spring, para probarla
 * con JUnit sin base de datos. Las reglas de los umbrales son las mismas que exige
 * {@link com.evolua.adaptation.ParametrosMotor}, de modo que un valor aceptado aquí nunca hará fallar al motor.
 */
public final class ValidadorConfiguracion {

    private ValidadorConfiguracion() {
    }

    /**
     * Valida el conjunto completo de parámetros tal como quedaría después de un cambio.
     *
     * @throws IllegalArgumentException si algún valor incumple su regla
     */
    public static void validar(Map<String, String> valores) {
        Integer refuerzo = entero(valores, ConfiguracionService.UMBRAL_REFUERZO);
        Integer dominio = entero(valores, ConfiguracionService.UMBRAL_DOMINIO);
        if (refuerzo != null) {
            rango(ConfiguracionService.UMBRAL_REFUERZO, refuerzo, 0, 99);
        }
        if (dominio != null) {
            rango(ConfiguracionService.UMBRAL_DOMINIO, dominio, 1, 100);
        }
        if (refuerzo != null && dominio != null && refuerzo >= dominio) {
            throw new IllegalArgumentException("El umbral de refuerzo debe ser menor que el umbral de dominio");
        }
        Integer minimo = entero(valores, ConfiguracionService.MIN_RESPUESTAS_TEMA);
        if (minimo != null) {
            rango(ConfiguracionService.MIN_RESPUESTAS_TEMA, minimo, 1, 100);
        }
        Integer intentos = entero(valores, ConfiguracionService.INTENTOS_PARA_DOMINIO);
        if (intentos != null) {
            rango(ConfiguracionService.INTENTOS_PARA_DOMINIO, intentos, 1, 10);
        }
        Integer practica = entero(valores, ConfiguracionService.PREGUNTAS_POR_PRACTICA);
        if (practica != null) {
            rango(ConfiguracionService.PREGUNTAS_POR_PRACTICA, practica, 1, 20);
        }
    }

    /** Devuelve el valor como entero, o null si el parámetro no está en el mapa. */
    private static Integer entero(Map<String, String> valores, String clave) {
        String valor = valores.get(clave);
        if (valor == null) {
            return null;
        }
        try {
            return Integer.valueOf(valor.trim());
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("El parámetro " + clave + " debe ser un número entero");
        }
    }

    private static void rango(String clave, int valor, int minimo, int maximo) {
        if (valor < minimo || valor > maximo) {
            throw new IllegalArgumentException(
                    "El parámetro " + clave + " debe estar entre " + minimo + " y " + maximo);
        }
    }
}
