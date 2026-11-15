package com.evolua.dto;

import java.math.BigDecimal;
import java.util.List;

/**
 * Reporte de rendimiento de un curso para su docente: indicadores generales, resultado por tema y por estudiante.
 */
public record RendimientoCursoResponse(Long cursoId, String cursoNombre, int estudiantesMatriculados,
        int estudiantesConActividad, long intentosFinalizados, BigDecimal promedioPuntuacion,
        List<RendimientoTemaResponse> temas, List<RendimientoEstudianteResponse> estudiantes) {
}
