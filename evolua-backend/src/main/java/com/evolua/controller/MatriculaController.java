package com.evolua.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.evolua.dto.MatriculaRequest;
import com.evolua.dto.MatriculaResponse;
import com.evolua.security.UsuarioPrincipal;
import com.evolua.service.MatriculaService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

/**
 * Matrículas de los estudiantes en un curso. Disponible para el docente responsable del curso y para los
 * administradores.
 */
@RestController
@RequestMapping("/api/v1/courses/{courseId}/enrollments")
@Tag(name = "Matrículas")
@SecurityRequirement(name = "bearerAuth")
@PreAuthorize("hasAnyRole('DOCENTE', 'ADMIN')")
public class MatriculaController {

    private final MatriculaService matriculaService;

    public MatriculaController(MatriculaService matriculaService) {
        this.matriculaService = matriculaService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Matricula a un estudiante en el curso, identificado por su correo")
    public MatriculaResponse matricular(@PathVariable("courseId") Long courseId,
            @Valid @RequestBody MatriculaRequest solicitud,
            @Parameter(hidden = true) @AuthenticationPrincipal UsuarioPrincipal usuario) {
        return matriculaService.matricular(courseId, solicitud, usuario);
    }

    @GetMapping
    @Operation(summary = "Lista los estudiantes matriculados en el curso")
    public List<MatriculaResponse> listar(@PathVariable("courseId") Long courseId,
            @Parameter(hidden = true) @AuthenticationPrincipal UsuarioPrincipal usuario) {
        return matriculaService.listar(courseId, usuario);
    }

    @DeleteMapping("/{studentId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Elimina la matrícula de un estudiante en el curso")
    public void eliminar(@PathVariable("courseId") Long courseId, @PathVariable("studentId") Long studentId,
            @Parameter(hidden = true) @AuthenticationPrincipal UsuarioPrincipal usuario) {
        matriculaService.eliminar(courseId, studentId, usuario);
    }
}
