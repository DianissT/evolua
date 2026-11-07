package com.evolua.adaptation;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Desempeño acumulado de un estudiante en un tema, calculado con los intentos finalizados.
 *
 * @param totalRespuestas respuestas acumuladas; las preguntas sin responder cuentan como incorrectas
 * @param respuestasCorrectas respuestas correctas acumuladas
 * @param intentosSatisfactorios intentos finalizados en los que el porcentaje del tema alcanzó el umbral de dominio
 */
public record EstadisticaTema(int totalRespuestas, int respuestasCorrectas, int intentosSatisfactorios) {

    public EstadisticaTema {
        if (totalRespuestas < 0 || respuestasCorrectas < 0 || intentosSatisfactorios < 0
                || respuestasCorrectas > totalRespuestas) {
            throw new IllegalArgumentException("La estadística del tema no es válida");
        }
    }

    public BigDecimal porcentaje() {
        return calcularPorcentaje(respuestasCorrectas, totalRespuestas);
    }

    /**
     * Porcentaje de aciertos con dos decimales; 0 si no hay respuestas.
     */
    public static BigDecimal calcularPorcentaje(int correctas, int total) {
        if (total == 0) {
            return BigDecimal.ZERO.setScale(2);
        }
        return BigDecimal.valueOf(correctas).multiply(BigDecimal.valueOf(100))
                .divide(BigDecimal.valueOf(total), 2, RoundingMode.HALF_UP);
    }

    /**
     * Indica si el porcentaje de aciertos alcanza el umbral, comparando con enteros para evitar errores de
     * redondeo.
     */
    public static boolean alcanza(int correctas, int total, int umbral) {
        return total > 0 && (long) correctas * 100 >= (long) umbral * total;
    }
}
