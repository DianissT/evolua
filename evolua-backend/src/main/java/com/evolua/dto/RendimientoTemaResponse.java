package com.evolua.dto;

import java.math.BigDecimal;

/**
 * Rendimiento acumulado de todos los estudiantes de un curso en un tema.
 */
public record RendimientoTemaResponse(Long temaId, String temaNombre, long totalRespuestas, long respuestasCorrectas,
        BigDecimal porcentajeAciertos, long estudiantesEnRefuerzo) {
}
