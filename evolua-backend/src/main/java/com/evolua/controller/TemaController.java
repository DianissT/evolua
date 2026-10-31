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

import com.evolua.dto.PreguntaResponse;
import com.evolua.dto.TemaRequest;
import com.evolua.dto.TemaResponse;
import com.evolua.security.UsuarioPrincipal;
import com.evolua.service.PreguntaService;
import com.evolua.service.TemaService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

/**
 * Gestión de temas y consulta de las preguntas de un tema. Reservado a los docentes del curso.
 */
@RestController
@RequestMapping("/api/v1/topics")
@Tag(name = "Temas")
@SecurityRequirement(name = "bearerAuth")
@PreAuthorize("hasRole('DOCENTE')")
public class TemaController {

    private final TemaService temaService;
    private final PreguntaService preguntaService;

    public TemaController(TemaService temaService, PreguntaService preguntaService) {
        this.temaService = temaService;
        this.preguntaService = preguntaService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Crea un tema en un curso del docente autenticado")
    public TemaResponse crear(@Valid @RequestBody TemaRequest solicitud,
            @Parameter(hidden = true) @AuthenticationPrincipal UsuarioPrincipal usuario) {
        return temaService.crear(solicitud, usuario.getId());
    }

    @PutMapping("/{id}")
    @Operation(summary = "Actualiza un tema")
    public TemaResponse actualizar(@PathVariable("id") Long id, @Valid @RequestBody TemaRequest solicitud,
            @Parameter(hidden = true) @AuthenticationPrincipal UsuarioPrincipal usuario) {
        return temaService.actualizar(id, solicitud, usuario.getId());
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Desactiva un tema; el tema no se elimina")
    public void desactivar(@PathVariable("id") Long id,
            @Parameter(hidden = true) @AuthenticationPrincipal UsuarioPrincipal usuario) {
        temaService.desactivar(id, usuario.getId());
    }

    @GetMapping("/{id}/questions")
    @Operation(summary = "Lista las preguntas de un tema, con sus opciones y la respuesta correcta")
    public List<PreguntaResponse> listarPreguntas(@PathVariable("id") Long id,
            @Parameter(hidden = true) @AuthenticationPrincipal UsuarioPrincipal usuario) {
        return preguntaService.listarPorTema(id, usuario.getId());
    }
}
