package com.evolua.controller;

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

import com.evolua.dto.PreguntaRequest;
import com.evolua.dto.PreguntaResponse;
import com.evolua.security.UsuarioPrincipal;
import com.evolua.service.PreguntaService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

/**
 * Gestión de preguntas y opciones del banco. Reservado a los docentes del curso.
 */
@RestController
@RequestMapping("/api/v1/questions")
@Tag(name = "Preguntas")
@SecurityRequirement(name = "bearerAuth")
@PreAuthorize("hasRole('DOCENTE')")
public class PreguntaController {

    private final PreguntaService preguntaService;

    public PreguntaController(PreguntaService preguntaService) {
        this.preguntaService = preguntaService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Crea una pregunta con sus opciones en un tema del docente autenticado")
    public PreguntaResponse crear(@Valid @RequestBody PreguntaRequest solicitud,
            @Parameter(hidden = true) @AuthenticationPrincipal UsuarioPrincipal usuario) {
        return preguntaService.crear(solicitud, usuario.getId());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Consulta una pregunta con sus opciones y la respuesta correcta")
    public PreguntaResponse obtener(@PathVariable("id") Long id,
            @Parameter(hidden = true) @AuthenticationPrincipal UsuarioPrincipal usuario) {
        return preguntaService.obtener(id, usuario.getId());
    }

    @PutMapping("/{id}")
    @Operation(summary = "Actualiza una pregunta; si ya tiene respuestas registradas no se puede cambiar su contenido")
    public PreguntaResponse actualizar(@PathVariable("id") Long id, @Valid @RequestBody PreguntaRequest solicitud,
            @Parameter(hidden = true) @AuthenticationPrincipal UsuarioPrincipal usuario) {
        return preguntaService.actualizar(id, solicitud, usuario.getId());
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Desactiva una pregunta; la pregunta no se elimina")
    public void desactivar(@PathVariable("id") Long id,
            @Parameter(hidden = true) @AuthenticationPrincipal UsuarioPrincipal usuario) {
        preguntaService.desactivar(id, usuario.getId());
    }
}
