package com.evolua.adaptation;

/**
 * Parámetros que controlan las decisiones del motor adaptativo. Se leen de la tabla de configuración.
 *
 * @param umbralRefuerzo porcentaje por debajo del cual un tema requiere refuerzo
 * @param umbralDominio porcentaje desde el cual un tema se considera avanzado
 * @param minRespuestasTema respuestas mínimas para clasificar un tema
 * @param intentosParaDominio resultados satisfactorios necesarios para declarar el dominio de un tema
 */
public record ParametrosMotor(int umbralRefuerzo, int umbralDominio, int minRespuestasTema, int intentosParaDominio) {

    public ParametrosMotor {
        if (umbralRefuerzo < 0 || umbralDominio > 100 || umbralRefuerzo >= umbralDominio) {
            throw new IllegalArgumentException(
                    "Los umbrales deben cumplir 0 <= umbral de refuerzo < umbral de dominio <= 100");
        }
        if (minRespuestasTema < 1 || intentosParaDominio < 1) {
            throw new IllegalArgumentException(
                    "Las respuestas mínimas y los intentos para el dominio deben ser al menos 1");
        }
    }
}
