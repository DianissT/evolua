package com.evolua.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Rendimiento de un estudiante matriculado en un curso. Si todavía no finalizó ningún intento, el promedio y la
 * última actividad son nulos.
 */
public record RendimientoEstudianteResponse(Long estudianteId, String nombre, String correo,
        long intentosFinalizados, BigDecimal promedioPuntuacion, LocalDateTime ultimaActividad,
        long temasEnRefuerzo) {
}
