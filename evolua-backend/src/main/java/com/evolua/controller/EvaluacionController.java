package com.evolua.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.evolua.dto.QuizRequest;
import com.evolua.dto.QuizResponse;
import com.evolua.dto.QuizResumenResponse;
import com.evolua.security.UsuarioPrincipal;
import com.evolua.service.EvaluacionService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

/**
 * Gestión y consulta de evaluaciones fijas.
 */
@RestController
@RequestMapping("/api/v1")
@Tag(name = "Evaluaciones")
@SecurityRequirement(name = "bearerAuth")
public class EvaluacionController {

    private final EvaluacionService evaluacionService;

    public EvaluacionController(EvaluacionService evaluacionService) {
        this.evaluacionService = evaluacionService;
    }

    @PostMapping("/quizzes")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('DOCENTE')")
    @Operation(summary = "Crea una evaluación fija con preguntas del curso")
    public QuizResponse crear(@Valid @RequestBody QuizRequest solicitud,
            @Parameter(hidden = true) @AuthenticationPrincipal UsuarioPrincipal usuario) {
        return evaluacionService.crear(solicitud, usuario.getId());
    }

    @GetMapping("/quizzes/{id}")
    @PreAuthorize("hasRole('DOCENTE')")
    @Operation(summary = "Consulta una evaluación fija con sus preguntas")
    public QuizResponse obtener(@PathVariable("id") Long id,
            @Parameter(hidden = true) @AuthenticationPrincipal UsuarioPrincipal usuario) {
        return evaluacionService.obtener(id, usuario.getId());
    }

    @PutMapping("/quizzes/{id}")
    @PreAuthorize("hasRole('DOCENTE')")
    @Operation(summary = "Actualiza una evaluación; si ya tiene intentos no se pueden cambiar sus preguntas")
    public QuizResponse actualizar(@PathVariable("id") Long id, @Valid @RequestBody QuizRequest solicitud,
            @Parameter(hidden = true) @AuthenticationPrincipal UsuarioPrincipal usuario) {
        return evaluacionService.actualizar(id, solicitud, usuario.getId());
    }

    @DeleteMapping("/quizzes/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasRole('DOCENTE')")
    @Operation(summary = "Desactiva una evaluación; la evaluación no se elimina")
    public void desactivar(@PathVariable("id") Long id,
            @Parameter(hidden = true) @AuthenticationPrincipal UsuarioPrincipal usuario) {
        evaluacionService.desactivar(id, usuario.getId());
    }

    @GetMapping("/courses/{courseId}/quizzes")
    @PreAuthorize("hasAnyRole('DOCENTE', 'ESTUDIANTE')")
    @Operation(summary = "Lista las evaluaciones de un curso; los estudiantes solo ven las activas")
    public List<QuizResumenResponse> listarPorCurso(@PathVariable("courseId") Long courseId,
            @Parameter(hidden = true) @AuthenticationPrincipal UsuarioPrincipal usuario) {
        return evaluacionService.listarPorCurso(courseId, usuario);
    }
}
