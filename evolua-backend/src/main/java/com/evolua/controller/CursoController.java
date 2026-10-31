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

import com.evolua.dto.CursoRequest;
import com.evolua.dto.CursoResponse;
import com.evolua.dto.TemaResponse;
import com.evolua.security.UsuarioPrincipal;
import com.evolua.service.CursoService;
import com.evolua.service.TemaService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

/**
 * Gestión y consulta de cursos y de sus temas.
 */
@RestController
@RequestMapping("/api/v1/courses")
@Tag(name = "Cursos")
@SecurityRequirement(name = "bearerAuth")
public class CursoController {

    private final CursoService cursoService;
    private final TemaService temaService;

    public CursoController(CursoService cursoService, TemaService temaService) {
        this.cursoService = cursoService;
        this.temaService = temaService;
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('DOCENTE', 'ESTUDIANTE')")
    @Operation(summary = "Lista los cursos del docente o los cursos activos en los que el estudiante está matriculado")
    public List<CursoResponse> listar(@Parameter(hidden = true) @AuthenticationPrincipal UsuarioPrincipal usuario) {
        return cursoService.listar(usuario);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('DOCENTE')")
    @Operation(summary = "Crea un curso del que el docente autenticado es responsable")
    public CursoResponse crear(@Valid @RequestBody CursoRequest solicitud,
            @Parameter(hidden = true) @AuthenticationPrincipal UsuarioPrincipal usuario) {
        return cursoService.crear(solicitud, usuario.getId());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('DOCENTE', 'ESTUDIANTE')")
    @Operation(summary = "Consulta un curso")
    public CursoResponse obtener(@PathVariable("id") Long id,
            @Parameter(hidden = true) @AuthenticationPrincipal UsuarioPrincipal usuario) {
        return cursoService.obtener(id, usuario);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('DOCENTE')")
    @Operation(summary = "Actualiza un curso del docente autenticado")
    public CursoResponse actualizar(@PathVariable("id") Long id, @Valid @RequestBody CursoRequest solicitud,
            @Parameter(hidden = true) @AuthenticationPrincipal UsuarioPrincipal usuario) {
        return cursoService.actualizar(id, solicitud, usuario.getId());
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasRole('DOCENTE')")
    @Operation(summary = "Desactiva un curso del docente autenticado; el curso no se elimina")
    public void desactivar(@PathVariable("id") Long id,
            @Parameter(hidden = true) @AuthenticationPrincipal UsuarioPrincipal usuario) {
        cursoService.desactivar(id, usuario.getId());
    }

    @GetMapping("/{id}/topics")
    @PreAuthorize("hasAnyRole('DOCENTE', 'ESTUDIANTE')")
    @Operation(summary = "Lista los temas de un curso; los estudiantes solo ven los temas activos")
    public List<TemaResponse> listarTemas(@PathVariable("id") Long id,
            @Parameter(hidden = true) @AuthenticationPrincipal UsuarioPrincipal usuario) {
        return temaService.listarPorCurso(id, usuario);
    }
}
