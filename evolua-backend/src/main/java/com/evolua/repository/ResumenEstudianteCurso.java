package com.evolua.repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Intentos finalizados de un estudiante en un curso, con la suma de sus puntuaciones (para calcular el promedio sin
 * perder precisión).
 */
public interface ResumenEstudianteCurso {

    Long getEstudianteId();

    Long getIntentos();

    BigDecimal getSuma();

    LocalDateTime getUltimaActividad();
}
