package com.evolua.dto;

import java.math.BigDecimal;

/**
 * Desempeño de un estudiante en un tema dentro de un intento.
 */
public record ResultadoTemaResponse(Long temaId, String temaNombre, int correctas, int total,
        BigDecimal porcentaje) {
}
