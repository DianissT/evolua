package com.evolua.controller;

import java.util.List;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.evolua.dto.PreguntaDificilResponse;
import com.evolua.dto.RendimientoCursoResponse;
import com.evolua.security.UsuarioPrincipal;
import com.evolua.service.ReporteService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

/**
 * Reportes de rendimiento de los cursos. Solo para docentes, y solo de sus propios cursos.
 */
@RestController
@RequestMapping("/api/v1/reports")
@Tag(name = "Reportes")
@SecurityRequirement(name = "bearerAuth")
@PreAuthorize("hasRole('DOCENTE')")
public class ReporteController {

    private final ReporteService reporteService;

    public ReporteController(ReporteService reporteService) {
        this.reporteService = reporteService;
    }

    @GetMapping("/courses/{id}/performance")
    @Operation(summary = "Rendimiento de un curso: indicadores generales, resultado por tema y por estudiante")
    public RendimientoCursoResponse rendimiento(@PathVariable("id") Long id,
            @Parameter(hidden = true) @AuthenticationPrincipal UsuarioPrincipal usuario) {
        return reporteService.rendimiento(id, usuario.getId());
    }

    @GetMapping("/courses/{id}/hard-questions")
    @Operation(summary = "Las diez preguntas del curso con menor porcentaje de aciertos; filtra por tema y por mínimo de respuestas")
    public List<PreguntaDificilResponse> preguntasDificiles(@PathVariable("id") Long id,
            @RequestParam(name = "min", defaultValue = "" + ReporteService.MINIMO_RESPUESTAS_POR_DEFECTO) int minimo,
            @RequestParam(name = "temaId", required = false) Long temaId,
            @Parameter(hidden = true) @AuthenticationPrincipal UsuarioPrincipal usuario) {
        return reporteService.preguntasDificiles(id, usuario.getId(), minimo, temaId);
    }
}
